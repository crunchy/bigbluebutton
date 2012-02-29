package com.crunchconnect.tests {
import com.crunchconnect.ErrorListener;

import flash.events.AsyncErrorEvent;
import flash.events.ErrorEvent;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.NetStatusEvent;
import flash.events.SecurityErrorEvent;

import org.bigbluebutton.common.red5.Connection;
import org.bigbluebutton.common.red5.ConnectionEvent;
import org.flexunit.asserts.assertEquals;

public class ErrorListenerTest {
    private var errorReporter:NullErrorReporter;
    private var errorListener:ErrorListener;
    private var event:Event;
    
    [Before]
    public function setUp():void {
        errorReporter = new NullErrorReporter({});
        errorListener = new ErrorListener(errorReporter);
    }

    [After]
    public function tearDown():void {
        event = null;
    }
    
    private function assertEventReports(event:Event, string:String):void {
        errorListener.onError(event);
        assertEquals(string, errorReporter.getLastMessage());        
    }

    [Test]
    public function testWithConnectionEvent():void {
        event = new ConnectionEvent(Connection.DISCONNECTED, false, false, "Disconnected");
        assertEventReports(event, "Connection Error: Disconnected");
    }

    [Test]
    public function testWithSecurityErrorEvent():void {
        event = new SecurityErrorEvent(SecurityErrorEvent.SECURITY_ERROR, false, false, "ERROR MESSAGE");
        assertEventReports(event, "Security Error: ERROR MESSAGE");
    }

    [Test]
    public function testWithAsyncErrorEvent():void {
        event = new AsyncErrorEvent(ErrorEvent.ERROR, false, false, "ERROR MESSAGE");
        assertEventReports(event, "Async Error: ERROR MESSAGE");
    }

    [Test]
    public function testWithIOErrorEvent():void {
        event = new IOErrorEvent(IOErrorEvent.IO_ERROR, false, false, "ERROR MESSAGE");
        assertEventReports(event, "IO Error: ERROR MESSAGE");
    }

    [Test]
    public function testWithNetStatusEvent():void {
        event = new NetStatusEvent(NetStatusEvent.NET_STATUS, false, false, {level: "status", code: "STATUS MESSAGE"});
        assertEventReports(event, null);

        event = new NetStatusEvent(NetStatusEvent.NET_STATUS, false, false, {level: "error", code: "ERROR MESSAGE"});
        assertEventReports(event, "NetStatusError: ERROR MESSAGE");
    }

    [Test]
    public function testWithUnknownError():void {
        event = new ErrorEvent(ErrorEvent.ERROR, false, false, "ERROR MESSAGE");
        assertEventReports(event, 'ErrorListener: Unknown event received [ErrorEvent type="error" bubbles=false cancelable=false eventPhase=2 text="ERROR MESSAGE"]');
    }
}
}
