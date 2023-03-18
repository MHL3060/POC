package local.tux.poc.cassandra.repository;

import local.tux.poc.cassandra.model.Category;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface CategoryRepository extends CassandraRepository<Category, String> {
}
