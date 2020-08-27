package org;

import java.util.concurrent.*;
import java.util.*;

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;

import javax.ws.rs.container.*;


@Path("fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {

        @Inject
        io.vertx.mutiny.pgclient.PgPool client;

        @Inject
        @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") 
        boolean schemaCreate;

        @PostConstruct
        void config() {
                if (schemaCreate) {
                   initdb();
                }
        }

        private void initdb() {
                client.query("DROP TABLE IF EXISTS fruits").execute()
                    .flatMap(r -> client.query("CREATE TABLE fruits (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
                    .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Orange')").execute())
                    .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Pear')").execute())
                    .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Apple')").execute())
                    .await().indefinitely();
        }
        
        @GET
        public Multi<Fruit> get() {
            return Fruit.findAll(client);
        }

        @GET
        @Path("{id}")
        public Uni<Response> getSingle(@PathParam Long id) {
            return Fruit.findById(client, id)
                .onItem().transform(fruit -> fruit != null ? Response.ok(fruit) : Response.status(Status.NOT_FOUND)) 
                .onItem().transform(ResponseBuilder::build); 
        }

        @POST
        public Uni<Response> create(Fruit fruit) {
            return fruit.save(client)
                .onItem().transform(id -> URI.create("/fruits/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
        }

        @PUT
        public Uni<Response> update(Fruit fruit) {
            return fruit.update(client)
                .onItem().transform(updated -> updated ? Status.OK : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
        }

        @DELETE
        @Path("{id}")
        public Uni<Response> delete(@PathParam Long id) {
            return Fruit.delete(client, id)
                .onItem().transform(deleted -> deleted ? Status.NO_CONTENT : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
        }
        
        @GET
        @Path("/cf")
        public CompletableFuture<Fruit> getCf() {
            CompletableFuture<Fruit> cf = CompletableFuture.supplyAsync(() -> {
                try{Thread.sleep(2000);} catch(Exception e) {}
                List<Fruit> list = Fruit.findAll(client)
                        .collectItems().asList().await().indefinitely();
                return list.get(0);
            });
            return cf;
        }
        
        @GET
        @Path("/cf2")
        public void getCf2(@Suspended AsyncResponse ar) {
            CompletableFuture<Fruit> cf = CompletableFuture.supplyAsync(() -> {
                try{Thread.sleep(2000);} catch(Exception e) {}
                List<Fruit> list = Fruit.findAll(client)
                        .collectItems().asList().await().indefinitely();
                return list.get(1);
            });
            cf.thenAccept(fruit -> ar.resume(Response.ok(fruit).build()));
        }
}
