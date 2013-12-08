REGISTER function.jar
raw_data = LOAD 'output' using JsonLoader('pi:int,id:int,sd:chararray,title:chararray,location:chararray,pj:int,ed:chararray,type:chararray');
B = GROUP raw_data BY id;
raw = FOREACH B{
        D = LIMIT raw_data 1;
        GENERATE FLATTEN(D);
};

group_types = GROUP raw BY type;
group_count = FOREACH group_types GENERATE AutoIncrease() AS id,COUNT(raw) AS total,SUM(raw.pi) AS pinum,SUM(raw.pj) AS pjnum ,group AS type;
group_avg = FOREACH group_count GENERATE id,MyAvg(total,pinum,pjnum) AS level, type AS type;
group_result = LIMIT group_avg 200;
STORE group_result INTO 'hbase://group' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage('info:level info:type');


locations = GROUP raw BY location;
location_count = FOREACH locations GENERATE AutoIncrease() AS id,COUNT(raw) AS total,SUM(raw.pi) AS pinum,SUM(raw.pj) AS pjnum, group AS location;
location_avg= FOREACH location_count GENERATE id,MyAvg(total,pinum,pjnum) AS level, location AS location;
location_result = LIMIT location_avg 200;
STORE location_result INTO 'hbase://location' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage('info:level info:location');


location_group = GROUP raw BY (location,type);
location_group_count = FOREACH location_group GENERATE AutoIncrease() AS id,group AS mgroup,COUNT(raw) AS total, SUM(raw.pi) AS pinum,SUM(raw.pj) AS pjnum;
location_group_avg = FOREACH location_group_count GENERATE id,FLATTEN(mgroup),MyAvg(total,pinum,pjnum) AS level;
location_group_result = LIMIT location_group_avg 200;
DUMP location_group_result;
STORE location_group_result INTO 'hbase://location_group' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage('lg:location lg:type info:group info:level');

year_season = FOREACH raw GENERATE Year(sd) AS year,Season(sd) AS season,pi,pj,type;
y_s_group = GROUP year_season BY (type,year,season);
y_s_group_count = FOREACH y_s_group GENERATE AutoIncrease() AS id,group AS mgroup,COUNT(year_season) AS total,SUM(year_season.pi) AS pinum,SUM(year_season.pj) AS pjnum;
y_s_group_avg = FOREACH y_s_group_count GENERATE id,FLATTEN(mgroup),MyAvg(total,pinum,pjnum) AS level;
STORE y_s_group_avg INTO 'hbase://y_s_group' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage('ysg:type ysg:year ysg:season info:group info:level');

seasons = FOREACH raw GENERATE Season(sd) AS season,pi,pj,location;
s_group = GROUP seasons BY (location,season);
s_group_count = FOREACH s_group GENERATE AutoIncrease() AS id,group AS mgroup,COUNT(seasons) AS total,SUM(seasons.pi) AS pinum,SUM(seasons.pj) AS pjnum;
s_group_avg = FOREACH s_group_count GENERATE id,FLATTEN(mygroup),MyAvg(total,pinum,pjnum) AS level;
STORE s_group_avg INTO 'hbase://s_group' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage('sga:location sga:season info:group info:level');
