package com.jthinking.jdbaudit.scan.password;

import com.jthinking.jdbaudit.db.api.DBSettings;
import com.jthinking.jdbaudit.db.api.entity.PasswordParam;
import com.jthinking.jdbaudit.scan.api.AbstractDBScanner;
import com.jthinking.jdbaudit.scan.api.entity.ScanTask;
import com.jthinking.jdbaudit.scan.api.entity.TaskControl;
import com.jthinking.jdbaudit.scan.password.data.DBPasswordData;
import com.jthinking.jdbaudit.scan.password.rule.WeakPasswordRule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * 新弱口令扫描器
 */
public class WeakPasswordScanner extends AbstractDBScanner<WeakPasswordRule, DBPasswordData> {

    private final static Pattern USER_PATTERN = Pattern.compile("<USER(\\(\\))?(?<op>(\\s*:\\s*(to_upper|to_lower|to_first_upper))*)>");

    private final static Pattern NUM_PATTERN = Pattern.compile("<NUM\\(\\s*(?<start>\\d+)\\s*,\\s*(?<end>\\d+)\\s*\\)>");

    @Override
    protected boolean match(WeakPasswordRule weekPassRule, DBPasswordData dbPassword, ScanTask scanTask) {
        TaskControl taskControl = scanTask.getTaskControl();
        DBSettings dbSettings = scanTask.getDbSettings();
        // 扫描停止
        if (taskControl.isStop()) {
            return false;
        }
        String mode = weekPassRule.getMode();
        if (mode.equals("text")) {
            String password = weekPassRule.getValue();
            if (dbSettings.comparePassword(new PasswordParam(password, dbPassword.getPassword(), dbPassword.getUsername()))) {
                // 返回实际密码
                dbPassword.setPassword(password);
                return true;
            }
            if (password.equals("") && dbPassword.getPassword().equals("")) {
                dbPassword.setPassword(password);
                return true;
            }
        } else if (mode.equals("pattern")) {
            String content = weekPassRule.getValue();
            Matcher userMatcher = USER_PATTERN.matcher(content);
            while (userMatcher.find()) {
                String replacement = dbPassword.getUsername();
                String opStr = userMatcher.group("op");
                String[] ops = opStr.split(":");
                for (String op : ops) {
                    op = op.trim();
                    switch (op) {
                        case "to_upper":
                            replacement = replacement.toUpperCase();
                            break;
                        case "to_lower":
                            replacement = replacement.toLowerCase();
                            break;
                        case "to_first_upper":
                            char[] cs = replacement.toCharArray();
                            if (cs.length > 0 && cs[0] >= 'a' && cs[0] <= 'z') {
                                cs[0] -= 32;
                            }
                            replacement = String.valueOf(cs);
                            break;
                    }
                }
                content = content.replace(userMatcher.group(), replacement);
            }

            Matcher numMatcher = NUM_PATTERN.matcher(content);

            if (numMatcher.find()) {
                int start = Integer.parseInt(numMatcher.group("start"));
                int end = Integer.parseInt(numMatcher.group("end"));
                for (int i = start; i <= end && !taskControl.isStop(); i++) {
                    if (i <= 0) {
                        continue;
                    }
                    StringBuilder builder = new StringBuilder();
                    for (int c = 0; c < i; c++) {
                        builder.append(9);
                    }
                    int endNumber = Integer.parseInt(builder.toString());
                    for (int j = 0; j <= endNumber && !taskControl.isStop(); j++) {
                        String format = String.format("%0" + i + "d", j);
                        String password = content.replace(numMatcher.group(), format);
                        if (dbSettings.comparePassword(new PasswordParam(password, dbPassword.getPassword(), dbPassword.getUsername()))) {
                            dbPassword.setPassword(password);
                            return true;
                        }
                    }
                }
            } else {
                if (dbSettings.comparePassword(new PasswordParam(content, dbPassword.getPassword(), dbPassword.getUsername()))) {
                    dbPassword.setPassword(content);
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    protected List<DBPasswordData> prepareData(DBSettings dbSettings, List<WeakPasswordRule> rules) {
        return dbSettings.getPassword().parallelStream()
                .map(dbPassword -> new DBPasswordData(dbPassword.getKey(), dbPassword.getUsername(), dbPassword.getPassword()))
                .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return "WEAK_PASSWORD";
    }
}
