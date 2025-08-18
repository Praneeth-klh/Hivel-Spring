package com.example.task.Service;

import com.example.task.model.Customer;
import com.example.task.repository.Customerepo;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BatchSer {
    @Autowired
    private Customerepo customerRepo;

    private final JobLauncher jobLauncher;
    private final Job importJob; // Renamed for clarity

    public BatchSer(JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.importJob = job;
    }

    // Existing import job method (unchanged)
    public String startJob(String filePath) {
        Path path = Path.of(filePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("CSV file does not exist: " + filePath);
        }

        int rowCount = countRows(filePath);
        int chunkSize;
        int threadLimit;

        if (rowCount <= 100) {
            chunkSize = 10; threadLimit = 3;
        } else if (rowCount <= 1000) {
            chunkSize = 200; threadLimit = 4;
        } else if (rowCount <= 5000) {
            chunkSize = 500; threadLimit = 8;
        } else {
            chunkSize = 1000; threadLimit = 10;
        }

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("filePath", filePath)
                    .addLong("chunkSize", (long) chunkSize)
                    .addLong("threadLimit", (long) threadLimit)
                    .addLong("startTime", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(importJob, params);

            return String.format(
                    "Batch job started. File: %s | Rows: %d | Chunk: %d | Threads: %d",
                    filePath, rowCount, chunkSize, threadLimit
            );
        } catch (Exception e) {
            throw new RuntimeException("Batch job failed to start: " + e.getMessage(), e);
        }
    }

    // Updated export job method with filters
    public String startExportJob(String exportPath, Map<String, Object> filters) {
        try {
            List<Customer> customers = fetchCustomersWithFilters(filters);

            if (customers.isEmpty()) {
                // Create empty CSV with headers
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportPath))) {
                    writer.write("ID,First Name,Last Name,Rating,Feedback\n");
                }
                return "Export completed - No data found matching filters";
            }

            // Use batch processing for large datasets
            exportDataInChunks(customers, exportPath, calculateOptimalChunkSize(customers.size()));

            return String.format("Export job completed successfully! %d records exported.", customers.size());

        } catch (IOException e) {
            throw new RuntimeException("Error during export: " + e.getMessage(), e);
        }
    }

    private List<Customer> fetchCustomersWithFilters(Map<String, Object> filters) {
        if (filters.isEmpty()) {
            return customerRepo.findAll();
        }

        // Apply filters (you might want to use Specifications or custom query methods)
        // For now, I'll show a simple approach - you can enhance this
        List<Customer> customers = customerRepo.findAll();

        return customers.stream()
                .filter(customer -> matchesFilters(customer, filters))
                .collect(Collectors.toList());
    }

    private boolean matchesFilters(Customer customer, Map<String, Object> filters) {
        if (filters.containsKey("firstName")) {
            String filterValue = (String) filters.get("firstName");
            if (!customer.getFirstName().toLowerCase().contains(filterValue.toLowerCase())) {
                return false;
            }
        }

        if (filters.containsKey("lastName")) {
            String filterValue = (String) filters.get("lastName");
            if (!customer.getLastName().toLowerCase().contains(filterValue.toLowerCase())) {
                return false;
            }
        }

        if (filters.containsKey("ratings")) {
            Integer filterValue = (Integer) filters.get("ratings");
            if (customer.getRating()!=filterValue) {
                return false;
            }
        }

        if (filters.containsKey("feedback")) {
            String filterValue = (String) filters.get("feedback");
            if (!customer.getFeedback().toLowerCase().contains(filterValue.toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    private int calculateOptimalChunkSize(int totalRecords) {
        if (totalRecords <= 1000) return 100;
        if (totalRecords <= 10000) return 500;
        if (totalRecords <= 100000) return 1000;
        return 2000;
    }

    public void exportDataInChunks(List<Customer> customers, String exportPath, int chunkSize) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportPath))) {
            // Write CSV header
            writer.write("ID,First Name,Last Name,Rating,Feedback\n");

            // Process in chunks to manage memory efficiently
            for (int i = 0; i < customers.size(); i += chunkSize) {
                int end = Math.min(i + chunkSize, customers.size());
                List<Customer> chunk = customers.subList(i, end);

                for (Customer customer : chunk) {
                    writer.write(formatCustomerToCSV(customer));
                }
                writer.flush(); // Flush after each chunk

                // Optional: Add small delay for very large datasets to prevent overwhelming
                if (customers.size() > 50000) {
                    try {
                        Thread.sleep(10); // 10ms delay
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private String formatCustomerToCSV(Customer customer) {
        return String.format("%d,\"%s\",\"%s\",%s,\"%s\"\n",
                customer.getId(),
                escapeCSVField(customer.getFirstName()),
                escapeCSVField(customer.getLastName()),
                customer.getRating(),
                escapeCSVField(customer.getFeedback()));
    }

    private String escapeCSVField(String field) {
        if (field == null) return "";
        return field.replace("\"", "\"\""); // Escape quotes for CSV
    }

    private int countRows(String filePath) {
        try (Stream<String> lines = Files.lines(Path.of(filePath))) {
            long count = lines.skip(1).count();
            return (int) count;
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file: " + filePath, e);
        }
    }
}
