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
//written by John Grden...

package org.bigbluebutton.common.red5 {
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.NetStatusEvent;
import flash.events.SecurityErrorEvent;
import flash.net.NetConnection;
import flash.net.ObjectEncoding;
import flash.utils.clearTimeout;
import flash.utils.setTimeout;

import mx.controls.Alert;

public class Connection extends EventDispatcher {
    public static const SUCCESS:String = "success";
    public static const FAILED:String = "failed";
    public static const CLOSED:String = "closed";
    public static const REJECTED:String = "rejected";
    public static const INVALIDAPP:String = "invalidApp";
    public static const APPSHUTDOWN:String = "appShutdown";
    public static const SECURITYERROR:String = "securityError";
    public static const DISCONNECTED:String = "disconnected";

    private static const CONNECTION_TIMEOUT:int = 15000;

    private var nc:NetConnection;
    private var uri:String;
    private var connectionTimer:Number = -1;

    public function Connection() {
        //  create the netConnection
        nc = new NetConnection();

        // set the encoding to AMF0 - still waiting for AMF3 to be implemented on Red5
        nc.objectEncoding = ObjectEncoding.AMF0;

        //  set it's client/focus to this
        nc.client = this;

        // add listeners for netstatus and security issues
        nc.addEventListener(NetStatusEvent.NET_STATUS, netStatusHandler);
        nc.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
    }

    private function startConnectionTimer():void {
        connectionTimer = setTimeout(connectionTimerExpired, CONNECTION_TIMEOUT);
    }

    private function connectionTimerExpired():void {
        if (!nc.connected) {
            var e:Event = new ConnectionEvent(Connection.DISCONNECTED, false, false, "Connection Timeout Expired");
            dispatchEvent(e);
        }
    }

    public function connect():void {
        if (getURI().length == 0) {
            Alert.show("please provide a valid URI connection string", "URI Connection String missing");
            return;
        } else if (nc.connected) {
            Alert.show("You are already connected to " + getURI(), "Already connected");
            return;
        }

        trace("[Connection] connect");
        nc.connect(getURI());
        startConnectionTimer();
    }

    public function close():void {
        nc.close();
    }

    public function setURI(p_URI:String):void {
        uri = p_URI;
    }

    public function getURI():String {
        return uri;
    }

    public function getConnection():NetConnection {
        return nc;
    }

    public function onBWDone():void {
        // have to have this for an RTMP connection
    }

    private function netStatusHandler(event:NetStatusEvent):void {
        var e:ConnectionEvent;

        trace("NETSTATUS EVENT: " + event.info.code + "; level: " + event.info.level);

        if (connectionTimer !== -1) {
            clearTimeout(connectionTimer);
            this.connectionTimer = -1;
        }

        switch (event.info.code) {
            case "NetConnection.Connect.Failed":
                e = new ConnectionEvent(Connection.FAILED, false, false, event.info.code);
                dispatchEvent(e);
                break;

            case "NetConnection.Connect.Success":
                e = new ConnectionEvent(Connection.SUCCESS, false, false, event.info.code);
                dispatchEvent(e);
                break;

            case "NetConnection.Connect.Rejected":
                e = new ConnectionEvent(Connection.REJECTED, false, false, event.info.code);
                dispatchEvent(e);
                break;

            case "NetConnection.Connect.Closed":
                e = new ConnectionEvent(Connection.CLOSED, false, false, event.info.code);
                dispatchEvent(e);
                break;

            case "NetConnection.Connect.InvalidApp":
                e = new ConnectionEvent(Connection.INVALIDAPP, false, false, event.info.code);
                dispatchEvent(e);
                break;

            case "NetConnection.Connect.AppShutdown":
                e = new ConnectionEvent(Connection.APPSHUTDOWN, false, false, event.info.code);
                dispatchEvent(e);
                break;
        }

        if (event.info.code != "NetConnection.Connect.Success") {
            // I dispatch DISCONNECTED incase someone just simply wants to know if we're not connected'
            // rather than having to subscribe to the events individually
            e = new ConnectionEvent(Connection.DISCONNECTED, false, false, event.info.code);
            dispatchEvent(e);
        }
    }

    private function securityErrorHandler(event:SecurityErrorEvent):void {
        var e:ConnectionEvent = new ConnectionEvent(Connection.SECURITYERROR, false, false, event.text);
        dispatchEvent(e);
    }
}
}
