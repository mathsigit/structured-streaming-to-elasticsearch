# structured-streaming-examples

This repository is created to add various examples of structured streaming to elasticsearch.


## How To build

First of all, need to install sbt, and execute the command:

```bash
sbt clean
sbt compile
sbt assembly
```

## How to execute with spark-submit

```bash
cd target/scala-2.11
spark-submit structured-streaming-to-elasticsearch-assembly-0.1.jar "/path/of/your/file/or/directory"
```

## Notice

* Path of file only supporting the '.txt' file.