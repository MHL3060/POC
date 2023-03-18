package local.tux.poc.cassandra.repository;

import local.tux.poc.cassandra.model.Lookup;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface LookupRepository extends CassandraRepository<Lookup, String> {


}
