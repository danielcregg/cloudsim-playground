# CloudSim Playground

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)
![Last Commit](https://img.shields.io/github/last-commit/danielcregg/cloudsim-playground?style=flat-square)

> **Note:** This repository is a fork of [crunchycookie/cloudsim-playground](https://github.com/crunchycookie/cloudsim-playground).

A Maven-based playground for the [CloudSim](https://github.com/Cloudslab/cloudsim) framework that simplifies the process of writing and running custom cloud simulation scenarios. This project eliminates the need for IDE-specific configurations by providing a clean, command-line-driven build and execution environment.

The project includes builder-pattern classes for constructing CloudSim entities (datacenters, hosts, characteristics) and a sample scenario that simulates a datacenter with 500 hosts.

## Prerequisites

- **Java** JDK 16 or higher
- **Apache Maven** 3.8.1 or higher
- **CloudSim JAR** -- `cloudsim-<version>.jar` from the [CloudSim releases](https://github.com/Cloudslab/cloudsim)

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/danielcregg/cloudsim-playground.git
   cd cloudsim-playground
   ```

2. **Configure the CloudSim JAR path:**

   Download the latest CloudSim release, extract the archive, and locate the `cloudsim-<version>.jar` file in the `jars/` directory. Then open `pom.xml` and update the `cloudsim-framework-jar-location` property with the absolute path to the JAR file:
   ```xml
   <cloudsim-framework-jar-location>/path/to/cloudsim-3.0.3.jar</cloudsim-framework-jar-location>
   ```

3. **Initialize the project:**
   ```bash
   mvn initialize
   ```
   This installs the CloudSim JAR into your local Maven repository.

## Usage

### Running the Built-in Scenario

Run the included `CreateDatacenter` scenario, which simulates a datacenter with 500 hosts (8 cores, 64 GB RAM, 10 TB storage each):

```bash
mvn exec:java@CreateDatacenterScenario
```

Or using the full class path:

```bash
mvn exec:java -Dexec.mainClass="org.crunchycookie.playground.cloudsim.examples.CreateDatacenter"
```

Expected output:
```
Initialising...
Successfully created the datacenter. Here are some stats.
Number of Hosts: 500. Let's check stats of a host.
Number of cores: 8
Amount of Ram(GB): 64
Amount of Storage(TB): 10
```

### Writing Custom Scenarios

1. Create a new Java class with a `main` method in:
   ```
   src/main/java/org/crunchycookie/playground/cloudsim/scenarios/
   ```

2. Optionally register it in `pom.xml` under the `exec-maven-plugin` configuration for a convenient alias:
   ```xml
   <execution>
       <id>MyCustomScenario</id>
       <configuration>
           <mainClass>org.crunchycookie.playground.cloudsim.scenarios.MyCustomScenario</mainClass>
       </configuration>
   </execution>
   ```

3. Run it:
   ```bash
   mvn exec:java@MyCustomScenario
   ```

## Project Structure

```
cloudsim-playground/
├── src/main/java/org/crunchycookie/playground/cloudsim/
│   ├── builders/
│   │   ├── DatacenterBuilder.java                  # Builder for Datacenter objects
│   │   ├── DatacenterCharacteristicsBuilder.java   # Builder for DatacenterCharacteristics
│   │   └── HostBuilder.java                        # Builder for Host objects
│   └── scenarios/
│       └── CreateDatacenter.java                   # Sample datacenter simulation
├── pom.xml
└── LICENSE
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

Original project by [crunchycookie](https://github.com/crunchycookie).
