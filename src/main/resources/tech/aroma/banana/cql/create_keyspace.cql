//Creates the Aroma Keyspace

CREATE KEYSPACE IF NOT EXISTS Aroma WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'DC1' : 3 };
