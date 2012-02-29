package com.crunchconnect {

import flash.net.URLLoader;
import flash.net.URLRequest;
import flash.net.URLRequestMethod;
import flash.net.URLVariables;
import flash.system.Capabilities;

public class ErrorReporter {
    internal const SOURCE:String = "ScreenShareFlashClient";
    internal const USER_AGENT:String = Capabilities.serverString;
    
    private var url:String;
    private var ip: String;
    private var user_id: String;
    private var room_id: String;
    private var room_type: String;

    private static function timestamp():Number {
        return (new Date()).getTime();
    }

    public function ErrorReporter(params:Object) {
        var host:String = params['host'];

        if (!host || host === "") {
            throw new ArgumentError("Host must be set");
        }

        url = "http://" + host + "/"; 
                
        ip         = params['ip'] || "";
        user_id    = params['user_id'] || "";
        room_type  = params['room_type'] || "";
        room_id    = params['room_id'] || "";
    }

    public function report(message:String):void  {
        var request:URLRequest = buildRequest(message)
          , loader:URLLoader   = new URLLoader(request);
        trace("ErrorReport#report", request);
        loader.load(request);
    }

    private function buildRequest(message:String):URLRequest {
        var request:URLRequest = new URLRequest(url);
        
        request.data = requestParams(message);
        request.method = URLRequestMethod.POST;
        
        return request;
    }

    private function requestParams(message:String):URLVariables {
        var params:URLVariables = new URLVariables();
    
        params.ip = ip;
        params.user_id = user_id;
        params.room_id = room_id;
        params.room_type = room_type;
        params.source = SOURCE;
        params.user_agent = USER_AGENT;
        params.message = message;
        params.timestamp = timestamp();

        return params;
    }
}
}
