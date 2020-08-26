package org;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.mutiny.Multi;

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
}
