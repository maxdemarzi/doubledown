package simulations

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import bootstrap._
import util.parsing.json.JSONArray
import scala.util.Random


class TestNodeCreation extends Simulation {
  val httpConf = httpConfig
    //.baseURL("http://localhost:7474/db/data")
    //.baseURL("http://ec2-50-16-149-207.compute-1.amazonaws.com:7474/db/data") // medium
    .baseURL("http://ec2-54-204-105-107.compute-1.amazonaws.com:7474/db/data")  // xlarge
    //.baseURL("http://54.205.124.234:7474/db/data")        // 2xl
    .acceptHeader("application/json")
    // Uncomment to see Requests
    //    .requestInfoExtractor(request => {
    //    println(request.getStringData)
    //    Nil
    //  })
    // Uncomment to see Response
    //    .responseInfoExtractor(response => {
    //    println(response.getResponseBody)
    //    Nil
    //  })
    .disableResponseChunksDiscarding

  val initialize = scenario("Prepare index")
    .exec(
      http("Create Schema Index")
        .post("/cypher")
        .body("""{"query": "CREATE INDEX ON :User(id)"}"""
    )
        .check(status.is(200))
    )


  val chooseRandomId = exec((session) => {
    val id: String = Random.nextInt(Integer.MAX_VALUE).toString()
    session.setAttribute("id", id)
  })

  val scn = scenario("Test Neo4j Node Creation")
    .during(60) {
     exec(chooseRandomId).
     exec(
      http("Post Create Via Cypher Parameterized")
        .post("/cypher")
        .body("""{"query": "CREATE (n:User { id : {id}})", "params": {"id": %s}}""".format("${id}"))
        .check(status.is(200))
      )
      .pause(0 milliseconds, 1 milliseconds)
  }
  //3600 request/minute

  val scn2 = scenario("Test Neo4j Node Creation without parameters")
    .during(60) {
    exec(chooseRandomId).
      exec(
        http("Post Create Via Cypher un-Parameterized")
          .post("/cypher")
          .body("""{"query": "CREATE (n:User { id : %s })"}""".format("${id}"))
          .check(status.is(200))
      )
      .pause(0 milliseconds, 1 milliseconds)
  }
  //3100 requests/minute

  val scn3 = scenario("Test Neo4j Node Creation Unindexed")
    .during(60) {
    exec(chooseRandomId).
      exec(
        http("Post Create Via Cypher Parameterized")
          .post("/cypher")
          .body("""{"query": "CREATE (n:User { id2 : {id}})", "params": {"id": %s}}""".format("${id}"))
          .check(status.is(200))
      )
      .pause(0 milliseconds, 1 milliseconds)
  }


  val scn4 = scenario("Test Neo4j Node Creation Unindexed without parameters")
    .during(60) {
    exec(chooseRandomId).
      exec(
        http("Post Create Via Cypher un-Parameterized")
          .post("/cypher")
          .body("""{"query": "CREATE (n:User { id2 : %s })"}""".format("${id}"))
          .check(status.is(200))
      )
      .pause(0 milliseconds, 1 milliseconds)
  }

  val scn5 = scenario("Test Neo4j Node Creation Unindexed Unlabeled")
    .during(60) {
    exec(chooseRandomId).
      exec(
        http("Post Create Via Cypher Parameterized")
          .post("/cypher")
          .body("""{"query": "CREATE (n { id3 : {id}})", "params": {"id": %s}}""".format("${id}"))
          .check(status.is(200))
      )
      .pause(0 milliseconds, 1 milliseconds)
  }

  val scn6 = scenario("Test Neo4j Node Creation Unindexed unlabeled without parameters")
    .during(60) {
    exec(chooseRandomId).
      exec(
        http("Post Create Via Cypher un-Parameterized")
          .post("/cypher")
          .body("""{"query": "CREATE (n { id3 : %s })"}""".format("${id}"))
          .check(status.is(200))
      )
      .pause(0 milliseconds, 1 milliseconds)
  }

  val scn7 = scenario("Test Neo4j Node Creation Unindexed Unlabeled Transactional Endpoint")
    .during(60) {
    exec(chooseRandomId).
      exec(
        http("Post Create Via Cypher Parameterized")
          .post("/transaction/commit")
          .body("""{"statements": [{"statement": "CREATE (n { id4 : {id}})", "parameters": {"id": %s}}]}""".format("${id}"))
          .check(status.is(200))
      )
      .pause(0 milliseconds, 1 milliseconds)
  }

  val scn8 = scenario("Test Neo4j Node Creation Unindexed Unlabeled Node Endpoint")
    .during(60) {
    exec(chooseRandomId).
      exec(
        http("Post Create Via Node")
          .post("/node")
          .body("""{"id5": %s}""".format("${id}"))
          .check(status.is(201))
      )
      .pause(0 milliseconds, 1 milliseconds)
  }


  val scn9 = scenario("Test Neo4j Node Creation using Merge with Parameters")
    .during(60) {
    exec(chooseRandomId).
      exec(
        http("Post Create Via Cypher Merge Parameterized")
          .post("/cypher")
          .body("""{"query": "MERGE (n:User { id : %s })"}""".format("${id}"))
          .check(status.is(200))
      )
      .pause(0 milliseconds, 1 milliseconds)
  }


  setUp(
    initialize.users(1).protocolConfig(httpConf)
    //,scn.users(20).protocolConfig(httpConf)
    //,scn2.users(20).protocolConfig(httpConf)
    //,scn3.users(20).protocolConfig(httpConf)
    //,scn4.users(20).protocolConfig(httpConf)
    //,scn5.users(20).protocolConfig(httpConf)
    //,scn6.users(20).protocolConfig(httpConf)
    //,scn7.users(20).protocolConfig(httpConf)
    //,scn8.users(20).protocolConfig(httpConf)
    ,scn9.users(20).protocolConfig(httpConf)
  )
}
