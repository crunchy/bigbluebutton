package com.crunchconnect.tests {
import com.crunchconnect.ErrorReporter;

public class NullErrorReporter extends ErrorReporter {
    private var lastMessage:String;

    public function NullErrorReporter(options:Object) {
        options["host"] = "bogus.local";
        super(options);
    }
    
    override public function report(message:String):void {
        lastMessage = message;
    }

    public function getLastMessage():String {
        return lastMessage;
    }
}
}
