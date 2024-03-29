# jdbaudit-java 

jDBAudit Java Implementation.

## Installing

Maven

```xml
<dependency>
    <groupId>com.jthinking.jdbaudit</groupId>
    <artifactId>jdbaudit</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle

```
implementation 'com.jthinking.jdbaudit:jdbaudit:0.1.0'
```

## Usage

```
RiskScanner riskScanner = new RiskScanner();

// load rules
try (FileInputStream inputStream = new FileInputStream("jdbaudit-rules/AUDIT.json")) {
    String jsonRule = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    riskScanner.loadRule(jsonRule, false);
}

// create DBSettings
DBSettings dbSettings = new MySQLSettings("localhost", 3306, "root", "root");

// submit task
riskScanner.submitTask(ScanTask.of(dbSettings, RiskType.AUDIT, new ScanTaskHandler() {

    @Override
    public void onStart(ScanTask scanTask) {
        System.out.println("onStart:" + scanTask.getTaskId());
    }

    @Override
    public void onAlert(ScanTask scanTask, ScanResult result) {
        Alert alert = result.getAlert();
        Rule rule = alert.getRule();
        String data = alert.getData();
        String taskId = scanTask.getTaskId();
        System.out.println("onAlert:" + scanTask.getTaskId());
    }

    @Override
    public void onFinish(ScanTask scanTask) {
        System.out.println("onFinish:" + scanTask.getTaskId());
    }

    @Override
    public void onError(ScanTask scanTask, Throwable error) {
        System.out.println("onError:" + scanTask.getTaskId());
    }
}));

// waiting for async task finish
Thread.sleep(1000 * 60);

// stop scanner
riskScanner.stop();
```