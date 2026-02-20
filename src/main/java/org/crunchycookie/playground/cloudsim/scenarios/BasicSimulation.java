/*
 * Title:        CloudSim Playground - Basic Simulation
 * Description:  A complete CloudSim simulation that creates infrastructure,
 *               submits workloads, runs the simulation, and prints results.
 * Licence:      MIT
 *
 * Copyright (c) 2024, Daniel Cregg.
 */
package org.crunchycookie.playground.cloudsim.scenarios;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelStochastic;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.crunchycookie.playground.cloudsim.builders.DatacenterBuilder;
import org.crunchycookie.playground.cloudsim.builders.DatacenterCharacteristicsBuilder;
import org.crunchycookie.playground.cloudsim.builders.HostBuilder;

/**
 * A complete CloudSim simulation scenario that demonstrates the full lifecycle
 * of a cloud simulation:
 *
 * <ol>
 *   <li>Initialize the CloudSim framework</li>
 *   <li>Create a datacenter with multiple hosts (using the builder pattern)</li>
 *   <li>Create a broker to act on behalf of a cloud customer</li>
 *   <li>Submit VMs and cloudlets (computational tasks)</li>
 *   <li>Run the simulation</li>
 *   <li>Print results including execution times and resource usage</li>
 * </ol>
 *
 * <p>This fills the gap in the original cloudsim-playground which only created
 * a datacenter but never ran an actual simulation.</p>
 *
 * <h2>Infrastructure</h2>
 * <ul>
 *   <li>1 Datacenter with 4 hosts</li>
 *   <li>Each host: 4 cores (1000 MIPS each), 8 GB RAM, 1 TB storage</li>
 *   <li>4 VMs: 2 cores each, 1000 MIPS, 2 GB RAM</li>
 *   <li>8 Cloudlets: varying lengths (10000-40000 MI), 1 core each</li>
 * </ul>
 *
 * @author Daniel Cregg
 */
public class BasicSimulation {

    private static final int NUMBER_OF_HOSTS = 4;
    private static final int CORES_PER_HOST = 4;
    private static final int MIPS_PER_CORE = 1000;
    private static final int RAM_PER_HOST_MB = 8192;
    private static final int STORAGE_PER_HOST_MB = 1_000_000;
    private static final int BW_PER_HOST = 10_000;

    private static final int NUMBER_OF_VMS = 4;
    private static final int VM_MIPS = 1000;
    private static final int VM_PES = 2;
    private static final int VM_RAM_MB = 2048;
    private static final long VM_BW = 1000;
    private static final long VM_SIZE = 10_000;

    private static final int NUMBER_OF_CLOUDLETS = 8;
    private static final int CLOUDLET_PES = 1;
    private static final long CLOUDLET_FILE_SIZE = 300;
    private static final long CLOUDLET_OUTPUT_SIZE = 300;

    /**
     * Runs the basic simulation scenario.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        Log.printLine("==========================================================");
        Log.printLine("  BasicSimulation - CloudSim Playground");
        Log.printLine("==========================================================");
        Log.printLine();

        try {
            // Step 1: Initialize CloudSim
            int numberOfUsers = 1;
            CloudSim.init(numberOfUsers, Calendar.getInstance(), false);
            Log.printLine("CloudSim initialized.");

            // Step 2: Create the datacenter
            Datacenter datacenter = createDatacenter("Datacenter_0");
            Log.printLine("Datacenter created with " + datacenter.getHostList().size() + " hosts.");

            // Step 3: Create the broker
            DatacenterBroker broker = new DatacenterBroker("Broker_0");
            int brokerId = broker.getId();

            // Step 4: Create VMs
            List<Vm> vmList = createVms(brokerId);
            broker.submitVmList(vmList);
            Log.printLine("Submitted " + vmList.size() + " VMs.");

            // Step 5: Create Cloudlets
            List<Cloudlet> cloudletList = createCloudlets(brokerId);
            broker.submitCloudletList(cloudletList);
            Log.printLine("Submitted " + cloudletList.size() + " cloudlets.");

            // Step 6: Run the simulation
            Log.printLine();
            Log.printLine("Starting simulation...");
            CloudSim.startSimulation();
            CloudSim.stopSimulation();
            Log.printLine("Simulation finished.");

            // Step 7: Print results
            List<Cloudlet> finishedCloudlets = broker.getCloudletReceivedList();
            printCloudletResults(finishedCloudlets);
            printHostUtilization(datacenter);

        } catch (Exception e) {
            Log.printLine("ERROR: Simulation failed.");
            e.printStackTrace();
        }
    }

    /**
     * Creates a datacenter with the specified number of hosts using the builder pattern.
     *
     * @param name the datacenter name
     * @return the created datacenter
     * @throws Exception if datacenter creation fails
     */
    private static Datacenter createDatacenter(String name) throws Exception {
        List<Host> hosts = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            hosts.add(
                new HostBuilder(i)
                    .withMipsPerCore(MIPS_PER_CORE)
                    .withNumberOfCores(CORES_PER_HOST)
                    .withAmountOfRamInGBs(RAM_PER_HOST_MB / 1024)
                    .withAmountOfStorageInGBs(STORAGE_PER_HOST_MB / 1024)
                    .withBandwidth(BW_PER_HOST)
                    .build()
            );
        }

        DatacenterCharacteristics characteristics = new DatacenterCharacteristicsBuilder()
            .withSystemArchitecture("x86")
            .withOperatingSystem("Linux")
            .withVmm("Xen")
            .withTimeZoneOfTheLocation(10.0)
            .withCostPerUsingProcessing(3.0)
            .withCostPerUsingMemory(0.05)
            .withCostPerUsingStorage(0.001)
            .withCostPerUsingBandwidth(0.0)
            .withHosts(hosts)
            .build();

        return new DatacenterBuilder(name)
            .withDatacenterCharacteristics(characteristics)
            .withVmAllocationPolicy(new VmAllocationPolicySimple(hosts))
            .withSchedulingInterval(0)
            .build();
    }

    /**
     * Creates virtual machines.
     *
     * @param brokerId the ID of the broker that owns the VMs
     * @return the list of VMs
     */
    private static List<Vm> createVms(int brokerId) {
        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            Vm vm = new Vm(
                i, brokerId, VM_MIPS, VM_PES, VM_RAM_MB,
                VM_BW, VM_SIZE, "Xen", new CloudletSchedulerTimeShared()
            );
            vmList.add(vm);
        }
        return vmList;
    }

    /**
     * Creates cloudlets with varying computational lengths to simulate
     * a heterogeneous workload. Cloudlets are distributed across VMs
     * in a round-robin fashion.
     *
     * @param brokerId the ID of the broker that owns the cloudlets
     * @return the list of cloudlets
     */
    private static List<Cloudlet> createCloudlets(int brokerId) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        UtilizationModel utilizationModel = new UtilizationModelFull();

        // Base lengths: each cloudlet gets a different length for variety
        long[] lengths = {10_000, 15_000, 20_000, 25_000, 30_000, 35_000, 40_000, 20_000};

        for (int i = 0; i < NUMBER_OF_CLOUDLETS; i++) {
            Cloudlet cloudlet = new Cloudlet(
                i, lengths[i % lengths.length], CLOUDLET_PES,
                CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
                utilizationModel, utilizationModel, utilizationModel
            );
            cloudlet.setUserId(brokerId);
            // Round-robin VM assignment
            cloudlet.setVmId(i % NUMBER_OF_VMS);
            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }

    /**
     * Prints the cloudlet execution results in a formatted table.
     *
     * @param cloudletList the list of finished cloudlets
     */
    private static void printCloudletResults(List<Cloudlet> cloudletList) {
        DecimalFormat dft = new DecimalFormat("###.##");
        String indent = "    ";

        Log.printLine();
        Log.printLine("==========================================================");
        Log.printLine("  CLOUDLET EXECUTION RESULTS");
        Log.printLine("==========================================================");
        Log.printLine(
            String.format("%-12s %-10s %-15s %-8s %-12s %-12s %-12s",
                "Cloudlet ID", "Status", "Datacenter ID", "VM ID",
                "CPU Time", "Start Time", "Finish Time")
        );
        Log.printLine("----------------------------------------------------------");

        for (Cloudlet cloudlet : cloudletList) {
            Log.print(String.format("%-12d ", cloudlet.getCloudletId()));

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.printLine(String.format(
                    "%-10s %-15d %-8d %-12s %-12s %-12s",
                    "SUCCESS",
                    cloudlet.getResourceId(),
                    cloudlet.getVmId(),
                    dft.format(cloudlet.getActualCPUTime()),
                    dft.format(cloudlet.getExecStartTime()),
                    dft.format(cloudlet.getFinishTime())
                ));
            } else {
                Log.printLine("FAILED");
            }
        }
        Log.printLine("----------------------------------------------------------");

        // Summary statistics
        double totalCpuTime = 0;
        int successCount = 0;
        for (Cloudlet cloudlet : cloudletList) {
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                totalCpuTime += cloudlet.getActualCPUTime();
                successCount++;
            }
        }
        Log.printLine();
        Log.printLine("  Total cloudlets: " + cloudletList.size());
        Log.printLine("  Successful:      " + successCount);
        Log.printLine("  Average CPU Time: " + dft.format(totalCpuTime / Math.max(successCount, 1)) + " seconds");
        Log.printLine();
    }

    /**
     * Prints host utilization statistics for all hosts in the datacenter.
     *
     * @param datacenter the datacenter to report on
     */
    private static void printHostUtilization(Datacenter datacenter) {
        Log.printLine("==========================================================");
        Log.printLine("  HOST UTILIZATION");
        Log.printLine("==========================================================");

        @SuppressWarnings("unchecked")
        List<Host> hosts = datacenter.getHostList();
        for (Host host : hosts) {
            int totalPes = host.getNumberOfPes();
            int usedPes = totalPes - host.getNumberOfFreePes();
            double utilization = (double) usedPes / totalPes * 100;

            Log.printLine(String.format(
                "  Host %d: %d/%d PEs used (%.1f%% utilization), RAM: %d MB, VMs: %d",
                host.getId(), usedPes, totalPes, utilization,
                host.getRam(), host.getVmList().size()
            ));
        }
        Log.printLine("==========================================================");
        Log.printLine();
    }
}
