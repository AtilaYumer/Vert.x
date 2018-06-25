package io.vertx.book.message;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import rx.*;

public class HelloConsumerMicroservice extends AbstractVerticle {

    @Override
    public void start() {
        vertx.createHttpServer()
                .requestHandler(
                        req -> {
                            EventBus bus = vertx.eventBus();
                            Single<JsonObject> obs1 = bus
                                    .<JsonObject>rxSend("hello", "Atila")
                                    .map(Message::body);
                            Single<JsonObject> obs2 = bus
                                    .<JsonObject>rxSend("hello", "Yumer")
                                    .map(Message::body);
                            Single
                                    .zip(obs1, obs2, (atila, yumer) ->
                                            new JsonObject()
                                                    .put("Atila", atila.getString("message")
                                                            + " from "
                                                            + atila.getString("served-by"))
                                                    .put("Yumer", yumer.getString("message")
                                                            + " from "
                                                            + yumer.getString("served-by"))
                                    )
                                    .subscribe(
                                            x -> req.response().end(x.encodePrettily()),
                                            t -> {
                                                t.printStackTrace();
                                                req.response().setStatusCode(500)
                                                        .end(t.getMessage());
                                            }
                                    );
                        })
                .listen(8082);
    }

}
