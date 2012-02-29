package com.crunchconnect {
import flash.events.AsyncErrorEvent;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.NetStatusEvent;
import flash.events.SecurityErrorEvent;
import flash.utils.getQualifiedClassName;

import org.bigbluebutton.common.red5.ConnectionEvent;

public class ErrorListener {
    private var reporter:ErrorReporter;

    public function ErrorListener(reporter:ErrorReporter) {
        this.reporter = reporter;
    }

    public function onError(e:Event):void {
        var name:String = "on" + getQualifiedClassName(e).split(':').pop();

        if (hasOwnProperty(name)) {
            this[name](e);
        } else {
            reporter.report("ErrorListener: Unknown event received " + e.toString());
        }
    }

    public function onConnectionEvent(e:ConnectionEvent):void {
        reporter.report("Connection Error: " + e.code);
    }

    public function onSecurityErrorEvent(e:SecurityErrorEvent):void {
        reporter.report("Security Error: " + e.text);
    }

    public function onAsyncErrorEvent(e:AsyncErrorEvent):void {
        reporter.report("Async Error: " + e.text);
    }

    public function onNetStatusEvent(e:NetStatusEvent):void {
        if(e.info.level == "error") {
            reporter.report("NetStatusError: " + e.info.code);
        }
    }

    public function onIOErrorEvent(e:IOErrorEvent):void {
        reporter.report("IO Error: " + e.text);
    }
}
}
