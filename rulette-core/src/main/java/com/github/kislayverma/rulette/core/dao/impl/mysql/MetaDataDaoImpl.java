package com.github.kislayverma.rulette.core.dao.impl.mysql;

import com.github.kislayverma.rulette.core.dao.MetaDataDao;
import com.github.kislayverma.rulette.core.metadata.RuleSystemMetaData;
import com.github.kislayverma.rulette.core.metadata.RuleInputMetaData;
import com.github.kislayverma.rulette.core.ruleinput.type.RuleInputType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MetaDataDaoImpl extends BaseDaoImpl implements MetaDataDao {

    @Override
    public RuleSystemMetaData getRuleSystemMetaData(String ruleSystemName) throws Exception {
        Statement statement = getConnection().createStatement();
        ResultSet resultSet =
            statement.executeQuery("SELECT * FROM rule_system WHERE name LIKE '" + ruleSystemName + "'");

        if (!resultSet.first()) {
            throw new Exception("No meta data found for rule system name : " + ruleSystemName);
        }

        RuleSystemMetaData metaData = new RuleSystemMetaData(
            resultSet.getString("name"),
            resultSet.getString("table_name"),
            resultSet.getString("unique_id_column_name"),
            resultSet.getString("output_column_name"),
            getInputs(ruleSystemName));

        return metaData;
    }

    private List<RuleInputMetaData> getInputs(String ruleSystemName) throws SQLException, Exception {
        List<RuleInputMetaData> inputs = new ArrayList<>();

        Statement statement = getConnection().createStatement();
        ResultSet resultSet =
                statement.executeQuery("SELECT b.* "
                + "FROM rule_system AS a "
                + "JOIN rule_input AS b "
                + "    ON b.rule_system_id = a.id "
                + "WHERE a.name LIKE '" + ruleSystemName + "' "
                + "ORDER BY b.priority ASC ");

        while (resultSet.next()) {
            RuleInputType ruleType =
                    "Value".equalsIgnoreCase(resultSet.getString("rule_type"))
                    ? RuleInputType.VALUE : RuleInputType.RANGE;
            String dataType = resultSet.getString("data_type").toUpperCase();

            inputs.add(new RuleInputMetaData(resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getInt("priority"),
                    ruleType,
                    dataType));
        }

        return inputs;
    }
}