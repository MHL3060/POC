package local.tux.poc;

import local.tux.poc.cassandra.CassandraConfiguration;
import local.tux.poc.cassandra.model.Category;
import local.tux.poc.cassandra.model.Lookup;
import local.tux.poc.cassandra.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.UpdateOptions;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Filter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = CassandraConfiguration.class)
@ActiveProfiles("test")
@EnableAutoConfiguration
public class CassandraLockingIT extends CassandraBaseIT {
    private final Logger logger = LoggerFactory.getLogger(CassandraLockingIT.class);

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CassandraTemplate cassandraTemplate;

    @Test
    public void loadContext() {
        assertTrue(true);
    }

    @Test
    public void testUsingVersionAnnotation() {
        var category = Category.builder()
                .id("id")
                .created(new Date())
                .name("name")
                .value("name")
                .build();
        categoryRepository.deleteById("id");
        categoryRepository.save(category);

        var categoryOpt = categoryRepository.findById("id");
        assertEquals("id", categoryOpt.get().getId());

        category = categoryOpt.get().toBuilder()
                .name("updatedName")
                .build();

        categoryRepository.save(category);
        assertEquals(category.getVersion(), 0l);
    }

    @Test
    public void testCondition() {
        var lookup = Lookup.builder()
                .id("id")
                .created(new Date())
                .name("name")
                .value("name")
                .build();
        cassandraTemplate.insert(lookup);

        var option = UpdateOptions.builder()
                .ifCondition(Filter.from(Criteria.where("name").is("name")))
                .build();
        var updateResult = cassandraTemplate.update(lookup, option);
        assertTrue(updateResult.wasApplied());
        option = UpdateOptions.builder()
                .ifCondition(Filter.from(Criteria.where("name").is("id2")))
                .build();
        var updateResult2 = cassandraTemplate.update(lookup, option);
        assertFalse(updateResult2.wasApplied());

    }
}
