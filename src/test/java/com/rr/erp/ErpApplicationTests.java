package com.rr.erp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// CI has no environment properties, so supply the values application.yaml requires.
// The datasource connects lazily and the dialect is set explicitly, so no database is needed.
@SpringBootTest(properties = {
		"JWT_SECRET=b22f02fc5d83acb6abaf0fe62ec241e3fd296767c92c92eb1423559a788eb629",
		"DB_URL=jdbc:postgresql://database-1.cn0u24eg21dc.ap-south-1.rds.amazonaws.com/postgres",
		"DB_USERNAME=VenusAdmin",
		"DB_PASSWORD=Venus7#$RR6754"
})
class ErpApplicationTests {

	@Test
	void contextLoads() {
	}

}
