package com.dr.bankspringbatch;

import com.dr.bankspringbatch.dao.BankTransaction;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

    @Autowired private JobBuilderFactory jobBuilderFactory;
    @Autowired private StepBuilderFactory stepBuilderFactory;
    @Autowired private ItemReader <BankTransaction> bankTransactionItemReader;
    @Autowired private ItemWriter <BankTransaction> bankTransactionItemWriter;

//    @Autowired private ItemProcessor <BankTransaction, BankTransaction> bankTransactionItemProcessor;

    @Bean
    public Job bankJob () {
        Step step1 = stepBuilderFactory.get("step-load-data")
                .<BankTransaction, BankTransaction>chunk(100)
                .reader(bankTransactionItemReader)
//                .processor(bankTransactionItemProcessor)
                .processor(compositeItemProcessor())
                .writer(bankTransactionItemWriter)
                .build();
        return jobBuilderFactory.get("bank-data-loader-job")
//                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }

    @Bean
    public ItemProcessor<BankTransaction, BankTransaction> compositeItemProcessor() {

        List<ItemProcessor<BankTransaction, BankTransaction>> itemProcessors = new ArrayList<>();
        itemProcessors.add(bankItemProcessor());
        itemProcessors.add(bankItemAnalyticsProcessor());

        CompositeItemProcessor<BankTransaction, BankTransaction> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(itemProcessors);

        return compositeItemProcessor;
    }

    // equivalent a @component in BankTransactionItemAnalyticsProcessor
    @Bean
    public BankTransactionItemAnalyticsProcessor bankItemAnalyticsProcessor() {
        return new BankTransactionItemAnalyticsProcessor();
    }


    // equivalent a @component in BankTransactionItemProcessor
    @Bean
    public BankTransactionItemProcessor bankItemProcessor() {
        return new BankTransactionItemProcessor();
    }

    // pour les fichiers plate sous forme text
    @Bean
    public FlatFileItemReader <BankTransaction> flatFileItemReader (@Value("${inputFile}") Resource inputFile) {

        FlatFileItemReader <BankTransaction> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("FFIR-CSV-Reader");
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setResource(inputFile);
        flatFileItemReader.setLineMapper(lineMapper());
        return flatFileItemReader;
    }

    @Bean
    public LineMapper<BankTransaction> lineMapper() {

        DefaultLineMapper <BankTransaction> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "accountId", "strTransactionDate", "transactionType", "amount");
        lineMapper.setLineTokenizer(lineTokenizer);

        BeanWrapperFieldSetMapper <BankTransaction> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(BankTransaction.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

//    @Bean
//    public ItemProcessor <BankTransaction, BankTransaction> itemProcessor() {
//        return new ItemProcessor<BankTransaction, BankTransaction>() {
//            @Override
//            public BankTransaction process(BankTransaction bankTransaction) throws Exception {
//                // change the str date to date et l'enregistere dans l'objet
//                return null;
//            }
//        };
//    }

}
