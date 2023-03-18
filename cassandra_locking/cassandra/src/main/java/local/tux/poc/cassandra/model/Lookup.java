package local.tux.poc.cassandra.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.config.EnableCassandraAuditing;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Date;

@Table("lookup")
@EnableCassandraAuditing
@RequiredArgsConstructor
@Getter
@Builder(toBuilder = true)
public class Lookup {
    @Id
    @PrimaryKeyColumn(value = "id", type = PrimaryKeyType.PARTITIONED)
    private final String id;

    @Column("name")
    private final String name;
    @Column("value")
    private final String value;
    @Column("created")
    private final Date created;

}
