package org;

import io.smallrye.mutiny.*;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.*;

public class Fruit {
	
    public Long id;

    public String name;

    public Fruit() {
        // default constructor.
    }

    public Fruit(String name) {
        this.name = name;
    }

    public Fruit(Long id, String name) {
        this.id = id;
        this.name = name;
    }

	private static Fruit from(Row row) {
	    return new Fruit(row.getLong("id"), row.getString("name"));
	}
	
	
	public static Multi<Fruit> findAll(PgPool client) {
	    return client.query("SELECT id, name FROM fruits ORDER BY name ASC").execute()
	            .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
	            .onItem().transform(Fruit::from);
	}

        public static Uni<Fruit> findById(PgPool client, Long id) {
            return client.preparedQuery("SELECT id, name FROM fruits WHERE id = $1").execute(Tuple.of(id)) 
                    .onItem().transform(RowSet::iterator) 
                    .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null); 
        }

        public Uni<Long> save(PgPool client) {
            return client.preparedQuery("INSERT INTO fruits (name) VALUES ($1) RETURNING (id)").execute(Tuple.of(name))
                    .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
        }

        public static Uni<Boolean> delete(PgPool client, Long id) {
            return client.preparedQuery("DELETE FROM fruits WHERE id = $1").execute(Tuple.of(id))
                    .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1); 
        }
}
