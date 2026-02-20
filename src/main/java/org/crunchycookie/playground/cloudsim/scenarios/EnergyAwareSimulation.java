/*
 * Title:        CloudSim Playground - Energy-Aware Simulation
 * Description:  Demonstrates power-aware cloud simulation using CloudSim 3.0.3
 *               with PowerHost, PowerVm, and PowerDatacenter components.
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
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * An energy-aware simulation scenario using CloudSim 3.0.3's power package.
 * This scenario uses {@link PowerHost}, {@link PowerVm}, and
 * {@link PowerDatacenter} to model and measure energy consumption.
 *
 * <p>The simulation creates a datacenter with power-aware hosts using a
 * linear power model, submits VMs and cloudlets, and then reports the
 * energy consumption alongside the cloudlet execution results.</p>
 *
 * <h2>Infrastructure</h2>
 * <ul>
 *   <li>1 PowerDatacenter with 4 PowerHosts</li>
 *   <li>Each host: 4 cores (1000 MIPS each), 8 GB RAM</li>
 *   <li>Power model: Linear (35W idle, 50W max) per host</li>
 *   <li>4 PowerVMs with 2 cores each</li>
 *   <li>8 Cloudlets with full CPU utilization</li>
 * </ul>
 *
 * <h2>Energy Model</h2>
 * <p>Uses {@link PowerModelLinear} which computes power as:
 * P = staticPower + (maxPower - staticPower) * utilization</p>
 *
 * @author Daniel Cregg
 * @see PowerHost
 * @see PowerDatacenter
 * @see PowerModelLinear
 */
public class EnergyAwareSimulation {

    private static final int NUMBER_OF_HOSTS = 4;
    private static final int CORES_PER_HOST = 4;
    private static final int MIPS_PER_CORE = 1000;
    private static final int RAM_PER_HOST_MB = 8192;
    private static final long STORAGE_PER_HOST_MB = 1_000_000;
    private static final int BW_PER_HOST = 10_000;

    /** Maximum power consumption in Watts per host at 100% utilization. */
    private static final double MAX_POWER_WATTS = 50.0;

    /** Static (idle) power consumption in Watts per host at 0% utilization. */
    private static final double STATIC_POWER_WATTS = 35.0;

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

    /** Scheduling interval in seconds for power measurement. */
    private static final double SCHEDULING_INTERVAL = 10.0;

    /**
     * Runs the energy-aware simulation scenario.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        Log.printLine("==========================================================");
        Log.printLine("  EnergyAwareSimulation - CloudSim Playground");
        Log.printLine("==========================================================");
        Log.printLine();

        try {
            // Initialize CloudSim
            CloudSim.init(1, Calendar.getInstance(), false);
            Log.printLine("CloudSim initialized.");

            // Create power-aware datacenter
            List<PowerHost> hostList = createPowerHosts();
            PowerDatacenter datacenter = createPowerDatacenter("PowerDatacenter_0", hostList);
            Log.printLine("PowerDatacenter created with " + hostList.size() + " power-aware hosts.");

            // Create broker
            PowerDatacenterBroker broker = new PowerDatacenterBroker("PowerBroker_0");
            int brokerId = broker.getId();

            // Create power-aware VMs
            List<Vm> vmList = createPowerVms(brokerId);
            broker.submitVmList(vmList);
            Log.printLine("Submitted " + vmList.size() + " PowerVMs.");

            // Create cloudlets
            List<Cloudlet> cloudletList = createCloudlets(brokerId);
            broker.submitCloudletList(cloudletList);
            Log.printLine("Submitted " + cloudletList.size() + " cloudlets.");

            // Run simulation
            Log.printLine();
            Log.printLine("Starting energy-aware simulation...");
            double lastClock = CloudSim.startSimulation();
            CloudSim.stopSimulation();
            Log.printLine("Simulation finished at time: " + new DecimalFormat("###.##").format(lastClock) + "s");

            // Print results
            List<Cloudlet> finishedCloudlets = broker.getCloudletReceivedList();
            printCloudletResults(finishedCloudlets);
            printEnergyConsumption(hostList, lastClock);

        } catch (Exception e) {
            Log.printLine("ERROR: Energy-aware simulation failed.");
            e.printStackTrace();
        }
    }

    /**
     * Creates power-aware hosts with a linear power model.
     *
     * @return the list of PowerHosts
     */
    private static List<PowerHost> createPowerHosts() {
        List<PowerHost> hostList = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_HOSTS; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < CORES_PER_HOST; j++) {
                peList.add(new Pe(j, new PeProvisionerSimple(MIPS_PER_CORE)));
            }

            // Linear power model: P = staticPower + (maxPower - staticPower) * utilization
            PowerModel powerModel = new PowerModelLinear(MAX_POWER_WATTS, STATIC_POWER_WATTS);

            PowerHost host = new PowerHost(
                i,
                new RamProvisionerSimple(RAM_PER_HOST_MB),
                new BwProvisionerSimple(BW_PER_HOST),
                STORAGE_PER_HOST_MB,
                peList,
                new VmSchedulerTimeShared(peList),
                powerModel
            );
            hostList.add(host);
        }
        return hostList;
    }

    /**
     * Creates a power-aware datacenter.
     *
     * @param name the datacenter name
     * @param hostList the list of power hosts
     * @return the PowerDatacenter
     * @throws Exception if creation fails
     */
    @SuppressWarnings("unchecked")
    private static PowerDatacenter createPowerDatacenter(
            String name, List<PowerHost> hostList) throws Exception {

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            "x86", "Linux", "Xen",
            hostList, 10.0,
            3.0, 0.05, 0.001, 0.0
        );

        return new PowerDatacenter(
            name,
            characteristics,
            new PowerVmAllocationPolicySimple(hostList),
            new LinkedList<Storage>(),
            SCHEDULING_INTERVAL
        );
    }

    /**
     * Creates power-aware VMs.
     *
     * @param brokerId the broker ID
     * @return the list of PowerVMs
     */
    private static List<Vm> createPowerVms(int brokerId) {
        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_VMS; i++) {
            // PowerVm constructor: id, brokerId, mips, pesNumber, ram, bw, size, priority, vmm, scheduler, schedulingInterval
            PowerVm vm = new PowerVm(
                i, brokerId, VM_MIPS, VM_PES, VM_RAM_MB,
                VM_BW, VM_SIZE, 1, "Xen",
                new CloudletSchedulerTimeShared(), SCHEDULING_INTERVAL
            );
            vmList.add(vm);
        }
        return vmList;
    }

    /**
     * Creates cloudlets with varying lengths.
     *
     * @param brokerId the broker ID
     * @return the list of cloudlets
     */
    private static List<Cloudlet> createCloudlets(int brokerId) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        UtilizationModel utilizationModel = new UtilizationModelFull();

        long[] lengths = {10_000, 15_000, 20_000, 25_000, 30_000, 35_000, 40_000, 20_000};

        for (int i = 0; i < NUMBER_OF_CLOUDLETS; i++) {
            Cloudlet cloudlet = new Cloudlet(
                i, lengths[i % lengths.length], CLOUDLET_PES,
                CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
                utilizationModel, utilizationModel, utilizationModel
            );
            cloudlet.setUserId(brokerId);
            cloudlet.setVmId(i % NUMBER_OF_VMS);
            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }

    /**
     * Prints cloudlet execution results.
     *
     * @param cloudletList the finished cloudlets
     */
    private static void printCloudletResults(List<Cloudlet> cloudletList) {
        DecimalFormat dft = new DecimalFormat("###.##");

        Log.printLine();
        Log.printLine("==========================================================");
        Log.printLine("  CLOUDLET EXECUTION RESULTS");
        Log.printLine("==========================================================");
        Log.printLine(String.format(
            "%-12s %-10s %-15s %-8s %-12s %-12s %-12s",
            "Cloudlet ID", "Status", "Datacenter ID", "VM ID",
            "CPU Time", "Start Time", "Finish Time"
        ));
        Log.printLine("----------------------------------------------------------");

        for (Cloudlet cloudlet : cloudletList) {
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.printLine(String.format(
                    "%-12d %-10s %-15d %-8d %-12s %-12s %-12s",
                    cloudlet.getCloudletId(), "SUCCESS",
                    cloudlet.getResourceId(), cloudlet.getVmId(),
                    dft.format(cloudlet.getActualCPUTime()),
                    dft.format(cloudlet.getExecStartTime()),
                    dft.format(cloudlet.getFinishTime())
                ));
            }
        }
        Log.printLine();
    }

    /**
     * Prints estimated energy consumption for each host.
     * The energy is estimated using the power model and the simulation duration.
     *
     * @param hostList the list of power hosts
     * @param simulationTime the total simulation time in seconds
     */
    private static void printEnergyConsumption(List<PowerHost> hostList, double simulationTime) {
        DecimalFormat dft = new DecimalFormat("###.##");

        Log.printLine("==========================================================");
        Log.printLine("  ENERGY CONSUMPTION REPORT");
        Log.printLine("==========================================================");
        Log.printLine(String.format(
            "  Simulation duration: %s seconds", dft.format(simulationTime)
        ));
        Log.printLine();
        Log.printLine(String.format(
            "  %-10s %-8s %-15s %-18s %-15s",
            "Host ID", "PEs", "Utilization(%)", "Est. Power (W)", "Est. Energy (Wh)"
        ));
        Log.printLine("  " + "--------------------------------------------------------------");

        double totalPower = 0;
        double totalEnergy = 0;

        for (PowerHost host : hostList) {
            int totalPes = host.getNumberOfPes();
            int usedPes = totalPes - host.getNumberOfFreePes();
            double utilization = (double) usedPes / totalPes;

            // Estimate power using the linear model
            double power;
            try {
                power = host.getPowerModel().getPower(utilization);
            } catch (Exception e) {
                power = STATIC_POWER_WATTS; // Fallback to idle power
            }

            // Energy in Watt-hours = Power(W) * Time(hours)
            double energyWh = power * (simulationTime / 3600.0);

            totalPower += power;
            totalEnergy += energyWh;

            Log.printLine(String.format(
                "  %-10d %-8s %-15s %-18s %-15s",
                host.getId(),
                usedPes + "/" + totalPes,
                dft.format(utilization * 100),
                dft.format(power),
                dft.format(energyWh)
            ));
        }

        Log.printLine("  " + "--------------------------------------------------------------");
        Log.printLine(String.format(
            "  %-10s %-8s %-15s %-18s %-15s",
            "TOTAL", "", "", dft.format(totalPower), dft.format(totalEnergy)
        ));
        Log.printLine();
        Log.printLine("  NOTE: Energy estimates assume constant utilization over the");
        Log.printLine("  simulation period. Actual energy depends on time-varying workload.");
        Log.printLine("==========================================================");
        Log.printLine();
    }
}
