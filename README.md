# local-flink-with-docker
Uses Apache Flink docker image with Kinesalite (and maybe Kafka)

1. Clone This Repo
1. [Install IntelliJ](https://www.jetbrains.com/help/idea/installation-guide.html)
1. [Install Docker](https://docs.docker.com/engine/install/)
1. Run included Docker Image
1. Set up local kinesis stream
1. Modify Code Samples
1. Run Code Samples



# Weekly  unique  weeklyAnalyser Job 
   path =>  src/main/java/myflinkapp/weeklyAnalyser.java

# Run the docker image
Once you've installed docker, either from your IntelliJ Terminal or your local machine's terminal, navigate to the git project's root and type the following:

```bash
docker-compose up -d
```

This will start your kinesalite process at the following url:
`https://localhost:4567`

The `-d` denotes a DAEMON process.

# Set up local Kinesis Stream

Execute the following two commands in succession in a terminal to create a local stream called `usersFlow` and then publish data under the `--data` field input.

```bash
aws kinesis create-stream --endpoint-url https://localhost:4567 --stream-name usersFlow --shard-count 6 --no-verify-ssl
```

```bash
aws kinesis put-record --endpoint-url https://localhost:4567 --stream-name usersFlow --data mytestdata --partition-key 123 --no-verify-ssl


# Monthly   unique  users  analyser Job
    src/main/java/myflinkapp/MonthlyAnalyser.java

      

```

Please note: the `ssl` folder in this repo is a test credential that is required for running kinesalite locally due to how the AWS CLI works.
