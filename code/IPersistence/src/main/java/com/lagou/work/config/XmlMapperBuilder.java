package com.lagou.work.config;

import com.lagou.work.pojo.Configuration;
import com.lagou.work.pojo.MappedStatement;
import com.lagou.work.pojo.SqlCommandType;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlMapperBuilder {

    private Configuration configuration;

    private Element rootElement;

    public XmlMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public void parse(InputStream inputStream) throws DocumentException {
        Document document = new SAXReader().read(inputStream);
        rootElement = document.getRootElement();
        Map<SqlCommandType, List<Element>> map = new HashMap<>();
        map.put(SqlCommandType.SELECT, rootElement.selectNodes("//select"));
        map.put(SqlCommandType.INSERT, rootElement.selectNodes("//insert"));
        map.put(SqlCommandType.UPDATE, rootElement.selectNodes("//update"));
        map.put(SqlCommandType.DELETE, rootElement.selectNodes("//delete"));
        common(map);
    }

    public void common(Map<SqlCommandType, List<Element>> map) {
        map.forEach((k, v) -> {
            for (Element element : v) {
                String id = element.attributeValue("id");
                String resultType = element.attributeValue("resultType");
                String paramterType = element.attributeValue("paramterType");
                String sqlText = element.getTextTrim();
                MappedStatement mappedStatement = new MappedStatement();
                mappedStatement.setId(id);
                mappedStatement.setParamterType(paramterType);
                mappedStatement.setResultType(resultType);
                mappedStatement.setSql(sqlText);
                mappedStatement.setSqlCommandType(k);
                configuration.getMappedStatementMap().put(rootElement.attributeValue("namespace") + "." + id, mappedStatement);
            }
        });

    }
}
