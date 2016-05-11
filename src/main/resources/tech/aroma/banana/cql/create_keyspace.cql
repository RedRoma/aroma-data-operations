//Creates the Aroma Keyspace

CREATE KEYSPACE IF NOT EXISTS Aroma WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'DC1' : 3 };

//Creates the Aroma Test Keyspace
CREATE KEYSPACE IF NOT EXISTS Aroma_Tests
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 }
;
