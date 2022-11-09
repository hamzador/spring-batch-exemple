package com.dr.bankspringbatch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class BankRestController {

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;

    @Autowired
    private BankTransactionItemAnalyticsProcessor bankTransactionItemAnalyticsProcessor;

//    @Autowired
//    public BankRestController(JobLauncher jobLauncher, Job job) {
//        this.jobLauncher = jobLauncher;
//        this.job = job;
//    }

    @GetMapping("/loadData")
    public BatchStatus load() throws Exception{
        Map<String, JobParameter> parameters = new HashMap<>();
        parameters.put("time", new JobParameter(System.currentTimeMillis()));
        JobParameters jobParameter = new JobParameters(parameters);
        JobExecution jobExecution = jobLauncher.run(job, jobParameter);
        while (jobExecution.isRunning()){
            System.out.println("....");
        }
        return jobExecution.getStatus();
    }

    @GetMapping("/analytics")
    public Map<String, Double> analytics() {

        Map<String, Double> result = new HashMap<>();
        result.put("totalCredit", bankTransactionItemAnalyticsProcessor.getTotalCredit());
        result.put("totalDebit",bankTransactionItemAnalyticsProcessor.getTotalDebit());

        return result;
    }
}
