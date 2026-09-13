package com.shiptrack.shiptrack_pro.config;

import com.shiptrack.shiptrack_pro.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

@Component
@RequiredArgsConstructor
public class RouteDataMigration implements CommandLineRunner {
    private final RouteRepository routeRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("DO $$ DECLARE constraint_name text; BEGIN "
                + "SELECT tc.constraint_name INTO constraint_name FROM information_schema.table_constraints tc "
                + "JOIN information_schema.constraint_column_usage ccu ON tc.constraint_name = ccu.constraint_name "
                + "WHERE tc.table_name = 'routes' AND tc.constraint_type = 'UNIQUE' "
                + "AND ccu.column_name = 'shipment_id' LIMIT 1; "
                + "IF constraint_name IS NOT NULL THEN EXECUTE 'ALTER TABLE routes DROP CONSTRAINT ' || quote_ident(constraint_name); END IF; "
                + "END $$;");
        routeRepository.markLegacyRoutesCurrent();
    }
}