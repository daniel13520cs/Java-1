package edu.nyu.cs9053.homework12;

import edu.nyu.cs9053.homework12.model.Region;
import edu.nyu.cs9053.homework12.model.VehicleMake;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * User: blangel
 * Date: 12/7/16
 * Time: 8:25 AM
 *
 * Convert Java 1.7 (and below) style code into Java 1.8 style code
 */
public class Conversions {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * @param values to remove empty values
     * @return non-empty list of {@code values}
     */
    public static List<String> removeEmptyValues(List<String> values) {
        if (values == null) {
            return null;
        }
        List<String> nonEmpty = new ArrayList<>(values.size());
        for (String value : values) {
            if ((value == null) || value.trim().isEmpty()) {
                continue;
            }
            nonEmpty.add(value.trim());
        }
        return nonEmpty;
    }

    /**
     * Java 8 equivalent of {@link #removeEmptyValues(List)}
     * This should <b>not</b> be parallel
     */
    public static List<String> removeEmptyValuesJava8(List<String> values) {
	    List<String> nonEmpty = values.stream().filter(c -> c != null).filter(c -> !c.trim().isEmpty()).map(String::trim).collect(Collectors.toList());
        return nonEmpty;
    }

    /**
     * Note, the uniqueness should be by {@link VehicleMake} itself not its associated name.
     * @param vehicleLoader for which to load {@link VehicleMake} values by {@link Region#name()}
     * @return a unique {@link NavigableSet} of lower-case {@link VehicleMake#getName()} for all {@link Region#values()}
     */
    public static NavigableSet<String> getUniqueAndNavigableLowerCaseMakeNames(VehicleLoader vehicleLoader) {
        Region[] regions = Region.values();
        final CountDownLatch latch = new CountDownLatch(regions.length);

        final Set<VehicleMake> uniqueVehicleMakes = new HashSet<>();
        for (Region region : regions) {
            EXECUTOR.submit(new Runnable() {
                @Override public void run() {
                    List<VehicleMake> regionMakes = vehicleLoader.getVehicleMakesByRegion(region.name());
                    if (regionMakes != null) {
                        uniqueVehicleMakes.addAll(regionMakes);
                    }
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        }

        NavigableSet<String> navigableMakeNames = new ConcurrentSkipListSet<>();
        for (VehicleMake make : uniqueVehicleMakes) {
            if (make.getName() == null) {
                continue;
            }
            navigableMakeNames.add(make.getName().toLowerCase());
        }
        return navigableMakeNames;
    }

    /**
     * Java 8 equivalent of {@link #getUniqueAndNavigableLowerCaseMakeNames(VehicleLoader)}
     * This should <b>be</b> parallel
     */
    public static NavigableSet<String> getUniqueAndNavigableLowerCaseMakeNamesJava8(VehicleLoader vehicleLoader) {
	    Set<VehicleMake> uniqueVehicleMakes = new HashSet<>();
        Region[] regions = Region.values();
        for(Region region : regions){
            List<VehicleMake> regionMakes = vehicleLoader.getVehicleMakesByRegion(region.name());
            Set<VehicleMake> temp = regionMakes.stream().filter(c -> c != null).collect(Collectors.toSet());
            uniqueVehicleMakes.addAll(temp);
        }
        Set<String> navigableMakeNames = uniqueVehicleMakes.stream().filter(c -> c.getName() != null).map(VehicleMake::getName).map(String::toLowerCase).collect(Collectors.toSet());
        return new TreeSet(navigableMakeNames);
    }

}
