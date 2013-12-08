REGISTER avg.jar
raw_data = LOAD 'output' using JsonLoader('pi:int,id:int,sd:chararray,title:chararray,location:chararray,pj:int,ed:chararray,type:chararray');
B = GROUP raw_data BY id;
raw = FOREACH B{
        D = LIMIT raw_data 1;
        GENERATE FLATTEN(D);
};

group_types = GROUP raw BY type;
group_count = FOREACH group_types GENERATE COUNT(raw) AS total,SUM(raw.pi) AS pinum,SUM(raw.pj) AS pjnum ,group AS type;
group_avg = FOREACH group_count GENERATE MyAvg(total,pinum,pjnum) AS level, type AS type;
group_result = LIMIT group_avg 200;

locations = GROUP raw BY location;
location_count = FOREACH locations GENERATE COUNT(raw) AS total,SUM(raw.pi) AS pinum,SUM(raw.pj) AS pjnum, group AS location;
location_avg= FOREACH location_count GENERATE MyAvg(total,pinum,pjnum) AS level, location AS location;
location_result = LIMIT location_avg 200;

location_group = GROUP raw BY (location,type);
location_group_count = FOREACH location_group GENERATE group AS mgroup,COUNT(raw) AS total, SUM(raw.pi) AS pinum,SUM(raw.pj) AS pjnum;
location_group_avg = FOREACH location_group_count GENERATE mgroup,MyAvg(total,pinum,pjnum) AS level;
location_group_result = LIMIT location_group_avg 200;
DUMP location_group_result;
