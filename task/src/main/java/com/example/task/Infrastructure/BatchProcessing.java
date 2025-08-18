package com.example.task.Infrastructure;

import com.example.task.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchProcessing {

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> reader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        Assert.hasText(filePath, "JobParameter 'filePath' must not be null or empty");

        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1); // skip header

        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
        // in reader()
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setQuoteCharacter('"');                 // handle quoted text like "Loved it, recommended!"
// MUST match bean property names exactly:
        tokenizer.setNames("firstName", "lastName", "feedback", "rating");
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        reader.setLineMapper(lineMapper);
        reader.setStrict(true);
        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<Customer> writer(DataSource dataSource) throws Exception {
        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();
        // in writer()
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
// SQL columns can remain lowercase (database side), named params must match bean:
        writer.setSql("INSERT INTO customer (first_name, last_name, feedback, rating,is_deleted) " +
                "VALUES (:firstName, :lastName, :feedback, :rating,:deleted)");
        writer.setDataSource(dataSource);
        writer.afterPropertiesSet(); // ensure fully initialized
        return writer;
    }

    @Bean
    @JobScope
    public Step step(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager,
                     FlatFileItemReader<Customer> reader,
                     JdbcBatchItemWriter<Customer> writer,
                     @Value("#{jobParameters['chunkSize']}") Long chunkSizeParam,
                     @Value("#{jobParameters['threadLimit']}") Long threadLimitParam) {

        int chunkSize = (chunkSizeParam != null) ? chunkSizeParam.intValue() : 100;
        int threadLimit = (threadLimitParam != null) ? threadLimitParam.intValue() : 1;

        // when creating task executor:
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(threadLimit);

// StepBuilder:
        return new StepBuilder("csv-step", jobRepository)
                .<Customer, Customer>chunk(chunkSize, transactionManager)
                .reader(reader)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("csv-job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }
}
