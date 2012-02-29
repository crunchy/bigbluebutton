/**
 * BigBlueButton open source conferencing system - http://www.bigbluebutton.org/
 *
 * Copyright (c) 2010 BigBlueButton Inc. and by respective authors (see below).
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * BigBlueButton is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with BigBlueButton; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bigbluebutton.modules.deskshare.services {

import com.asfusion.mate.events.Dispatcher;
import com.crunchconnect.ErrorListener;
import com.crunchconnect.ErrorReporter;

import flash.errors.IOError;

import flash.events.AsyncErrorEvent;
import flash.events.IOErrorEvent;
import flash.events.NetStatusEvent;
import flash.events.SecurityErrorEvent;
import flash.media.Video;
import flash.net.NetConnection;
import flash.net.NetStream;
import flash.net.Responder;
import flash.net.SharedObject;

import org.bigbluebutton.common.red5.Connection;
import org.bigbluebutton.common.red5.ConnectionEvent;
import org.bigbluebutton.modules.deskshare.events.AppletStartedEvent;
import org.bigbluebutton.modules.deskshare.events.CursorEvent;
import org.bigbluebutton.modules.deskshare.events.ViewStreamEvent;

/**
 * The DeskShareProxy communicates with the Red5 deskShare server application
 * @author Snap
 *
 */
public class DeskshareService {
    private var responder:Responder;
    private var dispatcher:Dispatcher;

    private var conn:Connection;
    private var nc:NetConnection;
    private var ns:NetStream;
    private var deskSO:SharedObject;

    private var uri:String;
    private var errorListener:ErrorListener;

    public function DeskshareService() {
        dispatcher = new Dispatcher();
        responder = new Responder(onStreamIsPublishingResult, onStreamIsPublishingResultError);
    }

    // Public
    public function connect(uri:String):void {
        this.uri = uri;
        trace("Deskshare Service connecting to " + uri);

        conn = new Connection();
        conn.addEventListener(Connection.SUCCESS, connectionSuccessHandler);
        conn.addEventListener(Connection.DISCONNECTED, errorListener.onError);
        conn.addEventListener(Connection.SECURITYERROR, errorListener.onError);
        conn.setURI(uri);
        conn.connect();
    }

    public function initializeErrorHandler(errorReporter:ErrorReporter):void {
        errorListener = new ErrorListener(errorReporter);
    }

    public function attachVideo(video:Video, room:String):void {
        ns = new NetStream(nc);
        ns.addEventListener(NetStatusEvent.NET_STATUS, onNetStatus);
        ns.addEventListener(AsyncErrorEvent.ASYNC_ERROR, errorListener.onError);
        ns.addEventListener(SecurityErrorEvent.SECURITY_ERROR, errorListener.onError);
        ns.addEventListener(IOErrorEvent.IO_ERROR, errorListener.onError);
        ns.client = this;
        ns.bufferTime = 0;
        ns.receiveVideo(true);
        ns.receiveAudio(false);
        ns.play(room);

        video.attachNetStream(ns);
    }

    // Triggers keyframe on red5. Public via BBB.
    public function sendStartedViewingNotification():void {
        trace("Sending start viewing to server");
        nc.call("deskshare.startedToViewStream", null);
    }


    // EVENT HANDLERS
    // Connection connected - Setup SO, remote call to check stream
    /**
     * Called by server when client connects.
     */
    public function onBWDone():void {
        trace('onBWDone');
        // do nothing
    }

    // SO Handler - triggered in Red5
    public function appletStarted(videoWidth:Number, videoHeight:Number):void {
        trace("Got applet started", videoWidth, videoHeight);
        var event:AppletStartedEvent = new AppletStartedEvent();
        event.videoWidth = videoWidth;
        event.videoHeight = videoHeight;
        dispatcher.dispatchEvent(event);
    }

    // SO Handler - triggered in Red5
    public function deskshareStreamStopped():void {
        trace("Received deskshareStreamStopped");
        dispatcher.dispatchEvent(new ViewStreamEvent(ViewStreamEvent.STOP));
        ns.close();
    }

    // SO Handler - triggered in Red5
    public function mouseLocationCallback(x:Number, y:Number):void {
        var event:CursorEvent = new CursorEvent(CursorEvent.UPDATE_CURSOR_LOC_EVENT);
        event.x = x;
        event.y = y;
        dispatcher.dispatchEvent(event);
    }

    private function connectionSuccessHandler(e:ConnectionEvent):void {
        trace("Successully connection to " + uri);
        nc = conn.getConnection();

        deskSO = SharedObject.getRemote("deskSO", uri, false);
        deskSO.client = this;
        deskSO.connect(nc);

        trace("checking if desk share stream is publishing");
        nc.call("deskshare.checkIfStreamIsPublishing", responder);
    }

    private function onNetStatus(e:NetStatusEvent):void {
        switch (e.info.code) {
            case "NetStream.Play.Start":
                trace("NetStream.Publish.Start for broadcast stream");
                sendStartedViewingNotification();
                break;

            case "NetStream.Play.UnpublishNotify":
                trace("NetStream.Play.UnpublishNotify for broadcast stream");
                deskshareStreamStopped();
                break;
        }
        
        if (e.info.level == "error") {
            errorListener.onError(e);
        }
    }

    // called from responder for checkIfStreamIsPublishing
    private function onStreamIsPublishingResult(result:Object):void {
        trace("Responder");
        if (result != null && (result.publishing as Boolean)) {
            var width:Number = result.width as Number;
            var height:Number = result.height as Number;

            appletStarted(width, height)
        } else {
            trace("No deskshare stream being published");
        }
    }

    // called from responder for checkIfStreamIsPublishing when error
    private function onStreamIsPublishingResultError(status:Object):void {
        var event:ConnectionEvent = new ConnectionEvent(Connection.DISCONNECTED, true, false, "Error while trying to call remote method on server");
        errorListener.onError(event);
    }


    // used in BBB proper, needed to compile ==================== [AND BELOW!!!]
    public function getConnection():NetConnection {
        return nc;
    }

    public function handleStartModuleEvent(module:DeskShareModule):void {
        trace("Deskshare Module starting");
        connect(module.uri);
    }

    public function disconnect():void {
        if (nc != null) nc.close();
    }

    /**
     * Called by the server when a notification is received to start viewing the broadcast stream .
     * This method is called on successful execution of sendStartViewingNotification()
     *
     */
    public function startViewing(videoWidth:Number, videoHeight:Number):void {
        trace("startViewing invoked by server", videoWidth, videoHeight);

        var event:ViewStreamEvent = new ViewStreamEvent(ViewStreamEvent.START);
        event.videoWidth = videoWidth;
        event.videoHeight = videoHeight;

        dispatcher.dispatchEvent(event);
    }

    /**
     * Call this method to send out a room-wide notification to start viewing the stream
     *
     */
    public function sendStartViewingNotification(captureWidth:Number, captureHeight:Number):void {
        try {
            deskSO.send("startViewing", captureWidth, captureHeight);
        } catch (e:Error) {
            trace("error while trying to send start viewing notification");
        }
    }
}
}