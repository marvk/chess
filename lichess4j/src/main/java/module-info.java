module lichess4j {
    exports net.marvk.chess.lichess4j;

    requires lombok;
    requires org.apache.logging.log4j;
    requires org.apache.httpcomponents.httpasyncclient;
    requires gson;
    requires core;
    requires httpcore.osgi;
    requires org.apache.httpcomponents.httpclient;
}