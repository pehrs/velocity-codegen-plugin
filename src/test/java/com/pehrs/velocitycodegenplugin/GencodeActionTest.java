package com.pehrs.velocitycodegenplugin;


import java.io.StringWriter;
import java.util.List;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;

public class GencodeActionTest {

  @Test
  public void velocityTest() {
    VelocityContext context = new VelocityContext();
    context.put("StringUtils", org.apache.commons.lang3.StringUtils.class);
    context.put("classname", "ClassName");
    context.put("fields", List.of(
        new FieldDeclaration("java.lang.String", "name", false, false, false, false, false, true,
            false, false),
        new FieldDeclaration("java.math.BigDecimal", "value", false, false, false, false, false,
            true, false, false)
    ));
    StringWriter writer = new StringWriter();
    VelocityEngine engine = new VelocityEngine();
    engine.init();
    String template = """
// $classname
#foreach($field in $fields)
this.set$StringUtils.capitalize($field.name)(other.get$StringUtils.capitalize($field.name)());
#end
            """;
    engine.evaluate(context, writer, "LOG_TAG", template);

    // Assert.assertEquals("Username is matti", writer.toString());
    System.out.println(writer.toString());
  }

}