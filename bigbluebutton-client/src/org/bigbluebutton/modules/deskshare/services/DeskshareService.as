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

import flash.events.AsyncErrorEvent;
import flash.events.NetStatusEvent;

import flash.media.Video;
import flash.net.NetConnection;
import flash.net.NetStream;
import flash.net.Responder;
import flash.net.SharedObject;
import org.bigbluebutton.common.LogUtil;
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
    private var conn:Connection;
    private var nc:NetConnection;
    private var deskSO:SharedObject;
    private var responder:Responder;
    private var dispatcher:Dispatcher;

    private var uri:String;
    private var width:Number;
    private var height:Number;
    private var ns:NetStream;
    private var room:String;

    public function DeskshareService() {
        this.dispatcher = new Dispatcher();
    }

    public function handleStartModuleEvent(module:DeskShareModule):void {
        trace("Deskshare Module starting");
        connect(module.uri);
    }

    public function connect(uri:String):void {
        this.uri = uri;
        trace("Deskshare Service connecting to " + uri);
        conn = new Connection();
        conn.addEventListener(Connection.SUCCESS, connectionSuccessHandler);
        conn.addEventListener(Connection.DISCONNECTED, connectionFailedHandler);
        conn.setURI(uri);
        conn.connect();

        responder = new Responder(
                function (result:Object):void {
                    trace("Responder")
                    if (result != null && (result.publishing as Boolean)) {
                        width = result.width as Number;
                        height = result.height as Number;
                        LogUtil.debug("Desk Share stream is streaming [" + width + "," + height + "]");
                        var event:ViewStreamEvent = new ViewStreamEvent(ViewStreamEvent.START);
                        event.videoWidth = width;
                        event.videoHeight = height;
                        dispatcher.dispatchEvent(event);
                    } else {
                        trace("No deskshare stream being published");
                    }
                },
                function (status:Object):void {
                    trace("Error while trying to call remote mathod on server");
                }
        );
    }

    public function disconnect():void {
        if (nc != null) nc.close();
    }


    public function attachVideo(video:Video, room:String):void {
        ns = new NetStream(getConnection());
//            ns.addEventListener(NetStatusEvent.NET_STATUS, onNetStatus);
//            ns.addEventListener(AsyncErrorEvent.ASYNC_ERROR, onAsyncError);
        ns.client = this;
        ns.bufferTime = 0;
        ns.receiveVideo(true);
        ns.receiveAudio(false);
        this.room = room;
        ns.play(room);
        video.attachNetStream(ns);
    }



    private function connectionSuccessHandler(e:ConnectionEvent):void {
        trace("Successully connection to " + uri);
        nc = conn.getConnection();

        deskSO = SharedObject.getRemote("deskSO", uri, false);
        deskSO.client = this;
        deskSO.connect(nc);

        checkIfStreamIsPublishing();
    }

    public function getConnection():NetConnection {
        return nc;
    }

    public function connectionFailedHandler(e:ConnectionEvent):void {
        trace("connection failed to " + uri + " with message " + e.code);
        dispatcher.dispatchEvent(e);
    }

    /**
     * Called by server when client connects.
     */
    public function onBWDone():void {
        trace('onBWDone');
        // do nothing
    }

    public function appletStarted(videoWidth:Number, videoHeight:Number):void {
        trace("Got applet started", videoWidth, videoHeight);
        var event:AppletStartedEvent = new AppletStartedEvent();
        event.videoWidth = videoWidth;
        event.videoHeight = videoHeight;
        dispatcher.dispatchEvent(event);
    }

    public function deskshareStreamStopped():void {
        stopViewing();
    }

    public function mouseLocationCallback(x:Number, y:Number):void {
        var event:CursorEvent = new CursorEvent(CursorEvent.UPDATE_CURSOR_LOC_EVENT);
        event.x = x;
        event.y = y;
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

    public function sendStartedViewingNotification():void {
        trace("Sending start viewing to server");
        nc.call("deskshare.startedToViewStream", null);
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
     * Sends a notification through the server to all the participants in the room to stop viewing the stream
     *
     */
    public function sendStopViewingNotification():void {
        trace("Sending stop viewing notification to other clients.");
        try {
            deskSO.send("stopViewing");
        } catch (e:Error) {
            trace("could not send stop viewing notification");
        }
    }


    /**
     * Sends a notification to the module to stop viewing the stream
     * This method is called on successful execution of sendStopViewingNotification()
     *
     */
    public function stopViewing():void {
        trace("Received dekskshareStreamStopped");
        dispatcher.dispatchEvent(new ViewStreamEvent(ViewStreamEvent.STOP));
        ns.close();
    }

    /**
     * Check if anybody is publishing the stream for this room
     * This method is useful for clients which have joined a room where somebody is already publishing
     *
     */
    private function checkIfStreamIsPublishing():void {
        trace("checking if desk share stream is publishing");
        nc.call("deskshare.checkIfStreamIsPublishing", responder);
    }

    public function calculateEncodingDimensions(captureWidth:Number, captureHeight:Number):void {
        height = captureHeight;
        width = captureWidth;
    }

    private function onAsyncError(e:AsyncErrorEvent):void {
        trace("VideoWindow::asyncerror " + e.toString());
    }

    private function onNetStatus(e:NetStatusEvent):void {
        switch (e.info.code) {
            case "NetStream.Play.Start":
                trace("NetStream.Publish.Start for broadcast stream " + room);
                trace("Dispatching start viewing event");
                sendStartedViewingNotification();
                break;
            case "NetStream.Play.UnpublishNotify":
                trace("NetStream.Play.UnpublishNotify for broadcast stream " + room);
                stopViewing();
                break;
        }
    }

}
}