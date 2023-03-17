# 项目介绍
    本项目针对以往工作中所有使用到的工具类进行汇总
    包括,时间操作工具,文件操作工具,JDBC数据访问SDK,sftp访问工具,neo4j操作工具,经纬度计算转换工具等，，
    

# 环境依赖
    jdk 1.8


# 目录结构描述
     ├── ReadMe.md           // 帮助文档
     
     |──cn.skymw.muhu  //程序根目录 
     
       ├── character.CharacterUtil    // 中文转换拼音工具类
       
       ├── common             // 公共辅助类
       
       ├──     //文件操作工具
            |──FileUtil     // 普通文件(IO)流操作类
            |──CsvFiltUtil  //csv文件创建读取
            
       │── jdbc.spring    //JDBC数据访问SDK
       
       │── msutils     //sftp 操作工具
       
       │── neo4j       //neo4j 操作工具
       
       │── postions   //坐标转换工具
       
       │── times     //时间操作
       
       

# 使用说明
    在maven项目中的pom.xml 增加如下配置进行工具的引入
    <dependency>
      <groupId>io.github.skymw</groupId>
      <artifactId>MiHuTool</artifactId>
      <version>1.0.1</version>
    </dependency>
   
# 方法
   ##字符串操作
      1.汉字转拼音
      String s="你好";
      String r=CharacterUtil.getPinyin(s);
   
   ###文件操作
      写出普通文件
       String usdir=System.getProperty("user.dir");
       FileUtil fu=new FileUtil();
       List<String> lineList=new ArrayList<>();
       lineList.add("22222222222222222222222");
       lineList.add("22222222222222222222222");
       lineList.add("22222222222222222222222");
       lineList.add("33333333333333333333333");
       lineList.add("33333333333333333333333");
       lineList.add("33333333333333333333333");
       lineList.add("33333333333333333333333");
       String filename="file_"+ DateTimeUtil.getFormatDate(new Date(),DateTimeUtil.DATE_FORMAT)+".txt";
       fu.writeLineList(usdir+"/data/"+filename,lineList,"UTF-8");
     
       读取文件内容
       FileUtil fu=new FileUtil();
       List<String> rlist=fu.readFileByLines("D:\\ws_worker\\april-project\\MethodTest\\data\\file_2023-03-01.txt","UTF-8",0);
       System.out.println(rlist.toString());    
       
       写出csv文件
       String[] heards=new String[]{"row1","row2","row3"};
       List<String> line=new ArrayList<>();
       String result="aaa,bbb,ccc";
       line.add(result);
       String filename="file_"+ DateTimeUtil.getFormatDate(new Date(),DateTimeUtil.DATE_FORMAT)+".csv";
       CsvFileUtil.write_csv(filename,heards,line); 
       
       读取csv文件
       List<Map<String,String>> resmap=  CsvFileUtil.readCsv("D:\\ws_worker\\april-project\\MethodTest\\file_2023-03-01.csv");
       System.out.println(resmap.toString());
       
   ###数据库操作
       查询
       JdbcHelper jdbcHelper=JdbcHelper.build("url","user","pass");
       List<Map<String,Object>> rlists=jdbcHelper.query("select * from poptbl");
       System.out.println(rlists.toString());
       
       增加
       JdbcHelper jdbcHelper=JdbcHelper.build("url","user","pass");
       List<Map<String,Object>> rlsit=new ArrayList<>();
       Map<String,Object> map=new HashMap<>();
       map.put("pref_name","test1");
       map.put("population","test1_1");
       Map<String,Object> map1=new HashMap<>();
       map1.put("pref_name","test2");
       map1.put("population","test2_2");
       rlsit.add(map);
       rlsit.add(map1);
       jdbcHelper.batchInsert ("poptbl",rlsit,true);   
       
       改
       String sql="update poptbl set pref_name='test2_22' where pref_name='test2'";
       jdbcHelper.getJdbcTemplate().update(sql);
       
       删除
       String sql="delete from poptbl where pref_name='test2_22'";
       jdbcHelper.getJdbcTemplate().update(sql);
       
   
   ###时间操作
   #####格式化类型：
   ![image](img/time_1.jpg)   
   #####调用实例
      //方法第一个参数传入时间，第二个参数指定时间的格式化类型
      DateTimeUtil.getFormatDate(new Date(),DateTimeUtil.TIME_FORMAT);
      //其他方法
      1.获取前一天的时间
         getLastDate()
      2.返回两个时间相隔的天数
         differentDays(parm1,parm2) //参数为时间类型(date)
      3. 将时间字符串转化为日期格式字符串
         timeStrToDateStr(parm) //参数输入格式为 yyyyMMddHHmm
      4. 获取当前时间的前一小时
         getLastDateTimeStrYYYYMMDDHH() 返回格式yyyyMMddHHmm
      5. 获取当前系统时间戳
         getCurrTimestamp()
      6.得到本月末的时间的字符串，格式：yyyyMMddHHmmss
         getMonthEndStr()
         
   ###neo4j操作
      **节点创建实例**
        Neo4jHelper neo4jHelper=Neo4jHelper.build("neo4j://localhost:7687","neo4j","jado@1301");
        //创建节点
        List<Node> nodelist=new ArrayList<>();
        Map<String, Object> pro1=new HashMap<>();
        pro1.put("id","0001");
        pro1.put("name","张三");
        pro1.put("age",14);
        String[] labels=new String[]{"员工"};
        Node node=new Node(null,null);
        node.setLabels(labels);
        node.setProfiles(pro1);
        nodelist.add(node);
        neo4jHelper.createNode(nodelist); 
      
      **关系创建实例**
      Neo4jHelper neo4jHelper=Neo4jHelper.build("neo4j://localhost:7687","neo4j","jado@1301");
      Map<String,Object> pro=new HashMap<>();
      pro.put("id","0001");
      Map<String,Object> pro1=new HashMap<>();
      pro1.put("id","0002");
      Node node1=new Node(pro,"员工");
      Node node2=new Node(pro1,"经理");
      String labels_1 = StringUtils.join(node1.getLabels(), ':'),
              id_1 = (String) node1.getProfiles().get("id"),
              labels_2 = StringUtils.join(node2.getLabels(), ':'),
              id_2 = (String) node2.getProfiles().get("id");
      System.out.println(labels_1+","+labels_2+","+id_1+","+id_2);
      
      Map<String,Object> repPro=new HashMap<>();
      repPro.put("demo","@@###");
      Relationship relationship=new Relationship((repPro),"管理");
      neo4jHelper.createRelationship(node2,node1,relationship);
      
     **更新节点属性**
      updateNode(List<Node> nodes)  Node中必须提供id属性以及label，通过id和label匹配到节点
     
     **删除节点属性同时删除与节点关联的关系。**
     deleteNode(List<Node> nodes)   Node中必须提供id属性以及label，通过id和label匹配到节点
     
     **更新从现有节点node1到node2的所有关系的属性**
     updateRelationship(List<Relationship> relationships)  Relationship中必须提供id属性以及label，通过id和label匹配到关系
     
     **删除关系，不影响现有节点**
     deleteRelationship(List<Relationship> relationships)  Relationship中必须提供id属性以及label，通过id和label匹配到关系
             
###经纬度坐标操作

 
 # 版本更新记录
 ###### v1.0.1
    1.初步整合相关内容