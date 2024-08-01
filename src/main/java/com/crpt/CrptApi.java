package com.crpt;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final Gson gson = new Gson();
    private final RateLimiter rateLimiter;
    private final IsmpIntegration ismpIntegration;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.rateLimiter = new RateLimiter(timeUnit, requestLimit);
        this.ismpIntegration = new IsmpIntegration();
    }


    public void createDocument(Document document) throws InterruptedException, IOException {
        rateLimiter.acquire();
        ismpIntegration.sendPostRequest(document);
    }

    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

        Document document = CrptConverter.convert(api);

        try {
            api.createDocument(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class CrptConverter {
        public static Document convert(CrptApi api) {
            Document document = api.new Document();
            document.setDescription("sample");
            document.setDoc_id("123");
            document.setDoc_status("new");
            document.setDoc_type("LP_INTRODUCE_GOODS");
            document.setImportRequest(true);
            document.setOwner_inn("1234567890");
            document.setParticipant_inn("0987654321");
            document.setProducer_inn("1122334455");
            document.setProduction_date("2020-01-23");
            document.setProduction_type("type");
            document.setReg_date("2020-01-23");
            document.setReg_number("reg123");

            Product product = api.new Product();
            product.setCertificate_document("cert123");
            product.setCertificate_document_date("2020-01-23");
            product.setCertificate_document_number("certnum123");
            product.setOwner_inn("1234567890");
            product.setProducer_inn("1122334455");
            product.setProduction_date("2020-01-23");
            product.setTnved_code("123456");
            product.setUit_code("uit123");
            product.setUitu_code("uitu123");

            document.setProducts(List.of(product));
            return document;
        }
    }

    public static class RateLimiter {
        private final int requestLimit;
        private final TimeUnit timeUnit;
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        private final ReentrantLock lock = new ReentrantLock();

        public RateLimiter(TimeUnit timeUnit, int requestLimit) {
            this.timeUnit = timeUnit;
            this.requestLimit = requestLimit;

            scheduler.scheduleAtFixedRate(() -> {
                requestCount.set(0);
            }, 0, 1, timeUnit);
        }

        public void acquire() throws InterruptedException {
            lock.lock();
            try {
                while (requestCount.get() >= requestLimit) {
                    lock.unlock();
                    TimeUnit.MILLISECONDS.sleep(100);
                    lock.lock();
                }

                requestCount.incrementAndGet();
            } finally {
                lock.unlock();
            }
        }
    }

    private class IsmpIntegration {
        String host = "https://ismp.crpt.ru";
        String apiV3 = "/api/v3";
        String lkDocumentsCreateRequest = "/lk/documents/create";
        public IsmpIntegration(){
        }
        public void sendPostRequest(Document document) throws IOException {
            String json = gson.toJson(document);
            URL url = new URL(host + apiV3 + lkDocumentsCreateRequest);//TODO сделать урл-билдер
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to create document, response code: " + responseCode);
            }
        }
    }


    public class Document {

        public Document(){

        }

        private String description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;

        public String getDescription() {
            return description;
        }

        public String getDoc_id() {
            return doc_id;
        }

        public String getDoc_status() {
            return doc_status;
        }

        public String getDoc_type() {
            return doc_type;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public String getParticipant_inn() {
            return participant_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public String getProduction_type() {
            return production_type;
        }

        public List<Product> getProducts() {
            return products;
        }

        public String getReg_date() {
            return reg_date;
        }

        public String getReg_number() {
            return reg_number;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setDoc_id(String doc_id) {
            this.doc_id = doc_id;
        }

        public void setDoc_status(String doc_status) {
            this.doc_status = doc_status;
        }

        public void setDoc_type(String doc_type) {
            this.doc_type = doc_type;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public void setParticipant_inn(String participant_inn) {
            this.participant_inn = participant_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public void setProduction_type(String production_type) {
            this.production_type = production_type;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public void setReg_date(String reg_date) {
            this.reg_date = reg_date;
        }

        public void setReg_number(String reg_number) {
            this.reg_number = reg_number;
        }
    }

    public class Product {

        public Product(){

        }
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public String getCertificate_document() {
            return certificate_document;
        }

        public void setCertificate_document(String certificate_document) {
            this.certificate_document = certificate_document;
        }

        public String getCertificate_document_date() {
            return certificate_document_date;
        }

        public void setCertificate_document_date(String certificate_document_date) {
            this.certificate_document_date = certificate_document_date;
        }

        public String getCertificate_document_number() {
            return certificate_document_number;
        }

        public void setCertificate_document_number(String certificate_document_number) {
            this.certificate_document_number = certificate_document_number;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public String getTnved_code() {
            return tnved_code;
        }

        public void setTnved_code(String tnved_code) {
            this.tnved_code = tnved_code;
        }

        public String getUit_code() {
            return uit_code;
        }

        public void setUit_code(String uit_code) {
            this.uit_code = uit_code;
        }

        public String getUitu_code() {
            return uitu_code;
        }

        public void setUitu_code(String uitu_code) {
            this.uitu_code = uitu_code;
        }

        // Getters and setters for all fields
    }
}
