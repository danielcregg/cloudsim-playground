# CloudSim Playground

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)
![Last Commit](https://img.shields.io/github/last-commit/danielcregg/cloudsim-playground?style=flat-square)

> **Note:** This repository is a fork of [crunchycookie/cloudsim-playground](https://github.com/crunchycookie/cloudsim-playground).

A Maven-based playground for the [CloudSim](https://github.com/Cloudslab/cloudsim) framework (v3.0.3) that simplifies the process of writing and running custom cloud simulation scenarios. This project includes builder-pattern classes for constructing CloudSim entities and multiple ready-to-run simulation scenarios.

## Scenarios

### 1. CreateDatacenter (Original)
Creates a datacenter with 500 hosts but does not run a simulation. Useful for verifying infrastructure setup.

### 2. BasicSimulation (NEW)
A **complete end-to-end simulation** that fills the gap of having no runnable simulation in the original project:
- Creates a datacenter with 4 hosts (4 cores each, 1000 MIPS/core, 8 GB RAM)
- Creates 4 VMs (2 cores each) and 8 cloudlets with varying computational lengths
- Runs the full simulation lifecycle
- Prints cloudlet execution results (CPU time, start/finish times)
- Prints host utilization statistics

### 3. EnergyAwareSimulation (NEW)
An **energy-aware simulation** using CloudSim's power package:
- Uses `PowerHost`, `PowerVm`, and `PowerDatacenter` for energy modeling
- Applies a linear power model (35W idle, 50W full load) to each host
- Creates 4 power-aware VMs and 8 cloudlets
- Reports per-host power consumption (Watts) and estimated energy (Wh)
- Demonstrates how to measure energy efficiency in CloudSim 3.0.3

## Prerequisites

- **Java** JDK 16 or higher
- **Apache Maven** 3.8.1 or higher
- **CloudSim JAR** -- `cloudsim-3.0.3.jar` from the [CloudSim releases](https://github.com/Cloudslab/cloudsim)

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/danielcregg/cloudsim-playground.git
   cd cloudsim-playground
   ```

2. **Set up the CloudSim JAR** (choose one method):

   **Option A: Place in `lib/` directory (recommended)**
   ```bash
   mkdir -p lib
   cp /path/to/cloudsim-3.0.3.jar lib/
   ```
   The `pom.xml` default looks for `lib/cloudsim-3.0.3.jar` relative to the project root.

   **Option B: Specify a custom path**
   ```bash
   mvn initialize -Dcloudsim-framework-jar-location=/absolute/path/to/cloudsim-3.0.3.jar
   ```

3. **Initialize the project** (installs the CloudSim JAR to your local Maven repository):
   ```bash
   mvn initialize
   ```

4. **Build:**
   ```bash
   mvn compile
   ```

## Running the Scenarios

```bash
# Run the basic simulation (full lifecycle)
mvn exec:java@BasicSimulation

# Run the energy-aware simulation
mvn exec:java@EnergyAwareSimulation

# Run the original datacenter creation scenario
mvn exec:java@CreateDatacenterScenario

# Or run any scenario directly:
mvn exec:java -Dexec.mainClass="org.crunchycookie.playground.cloudsim.scenarios.BasicSimulation"
```

### Expected Output (BasicSimulation)
```
==========================================================
  BasicSimulation - CloudSim Playground
==========================================================

CloudSim initialized.
Datacenter created with 4 hosts.
Submitted 4 VMs.
Submitted 8 cloudlets.

Starting simulation...
Simulation finished.

==========================================================
  CLOUDLET EXECUTION RESULTS
==========================================================
Cloudlet ID  Status     Datacenter ID   VM ID    CPU Time     Start Time   Finish Time
----------------------------------------------------------
0            SUCCESS    2               0        10.0         0.1          10.1
...
```

## Project Structure

```
cloudsim-playground/
├── lib/                          # Place cloudsim-3.0.3.jar here
├── src/main/java/org/crunchycookie/playground/cloudsim/
│   ├── builders/
│   │   ├── DatacenterBuilder.java
│   │   ├── DatacenterCharacteristicsBuilder.java
│   │   └── HostBuilder.java
│   └── scenarios/
│       ├── CreateDatacenter.java          # Original: creates datacenter only
│       ├── BasicSimulation.java           # NEW: complete simulation lifecycle
│       └── EnergyAwareSimulation.java     # NEW: energy-aware with PowerHost
├── pom.xml
├── LICENSE
└── README.md
```

## Migrating to CloudSim Plus

For modern cloud simulation research, consider using [CloudSim Plus](https://github.com/danielcregg/cloudsimplus-daniel), which provides:
- Java 17+ support with modern API design
- Built-in power models and energy monitoring
- VM migration policies with utilization thresholds
- Extensive documentation and 90+ examples
- Active maintenance and community support

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

Original project by [crunchycookie](https://github.com/crunchycookie).
