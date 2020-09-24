

# How to run

### this is going to start the infrastructure
docker-compose -f assets/docker-compose/docker-compose-kafka.yml up -d
docker-compose -f assets/docker-compose/docker-compose-postgres.yml up -d

#### setup the tables!
docker exec -i docker-compose_db_1 bash < setup.psql

### this two are going to perform HTTP queries to get the data
sbt 'integration/runMain use_cases.highest_growing_countries_ranked_by_gdp.stage_1.GdpGet'
sbt 'integration/runMain use_cases.highest_growing_countries_ranked_by_gdp.stage_1.PopulationGet'

### this one is going to start an Akka Actor System
sbt 'integration/runMain use_cases.highest_growing_countries_ranked_by_gdp.stage_2.Writeside'
### this one is going to populate the global GDP ranking table 
sbt 'readside/runMain projection.CountryGrossDomesticProductGlobalRankingProjection'
### this one is going to populate the yearly population growth by country table 
sbt 'readside/runMain projection.CountryYearlyTotalPopulationDeltaProjection'

#### query them tables!
docker exec -i docker-compose_db_1 bash < query.psql

![](https://i.imgur.com/367h96b.png)
