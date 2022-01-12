# Start env

## Local with dev configuration
`docker-compose -f docker-compose.yml -f docker-compose-dev.yml up --build -d`

## Remote Docker with test configuration
`docker-compose -H oqcp-test-srv.minhiriathaen.hu:2375 -f docker-compose.yml -f docker-compose-dev-test.yml --env-file test.env up --build -d`

