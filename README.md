## Traitement de données en temps réel: bitcoin

![schéma topologie](https://github.com/carolio7/bitcoin_Real-time_Analysis/blob/master/architecture_fonctionnelle.png)

Il contient :

* le script [currency_price_producer.py]() qui permet de récupérer les cours du bitcoin en € - $ - £ via l'API [Coindesk](http://api.coindesk.com/v1/bpi/currentprice.json) puis d'envoyer les données au producer kafka 'bitcoin-price'.
* le script [btc_op_producer.py]() qui permet de récuperer les informations sur les opérations éffectuées en temps-réel du bitcoin via l'API [blockchain](https://blockchain.info/api/api_websocket)  et de les envoyer au producer kafka 'bitcoin-transax'.

* l'application storm [btcAnalytics-1.0-SNAPSHOT.jar]() qui récupere les données du kafka, traiter en temps-réel puis enregistrer les statistique dans un cluster elasticsearch.


### Installation

Commencez par télécharger et installer:

    [zookeeper](https://zookeeper.apache.org/)
    [Kafka](https://kafka.apache.org/)
    [storm](https://storm.apache.org/)
    [elasticsearch](https://elastic.co/downloads)
    [kibana](https://elastic.co/downloads)


Lancez les clusters par les commandes suivantes :

    cd <repertoire zookeeper> puis ./bin/zkServer.sh start-foreground
    cd <repertoire kafka> puis ./bin/kafka-server-start.sh ./config/server.properties
    cd <repertoire storm> puis ./bin/storm nimbus
    cd <repertoire storm> puis ./bin/storm supervisor
    cd <repertoire storm> puis ./bin/storm ui
    cd <repertoire elasticsearch> puis ./bin/elasticsearch
    cd <repertoire kibana> puis ./bin/kibana


Installation de kafka-manager est compliqué comparé aux autres outils:

    1- clonez l'installation à partir de git, dans bash:  [git clone https://github.com/yahoo/kafka-manager.git]
    
    2- dans le repertoire télechargé, lancez le packaging: [./sbt clean dist]
    
    3- aller dans le sous-répertoire: [cd target/universal/kafka-manager-2.0.0.2/]
    
    4- lancez kafka-manager : [ZK_HOSTS=localhost:2181 ./bin/kafka-manager]
    
    5- Ajoutez le cluster kafka dans [localhost:9000] :
            Cluster --> Add cluster --> Cluster Zookeeper Hosts (à renseigner l'adresse de kafka)





### Lancement des programmes


Chargez l'application jar dans le cluster storm : 

    /.../<repertoire zookeeper>/bin/storm jar btcAnalytics-1.0-SNAPSHOT.jar btcAnalytics.App remote

Puis lancez les deux scripts dédiés aux producers kafka :

    python ./currency_price_producer.py
    python ./btc_op_producer.py




### Visualization des résultat sur Kibana

Copiez-collez tout le lien ci-dessous dans votre navigateur afin d'obtenir le tableau de bord Kibana:

    http://localhost:5601/app/kibana#/dashboard/18f48800-a9f2-11ea-8d10-c7a5eafb804a?_g=(refreshInterval:(pause:!t,value:0),time:(from:now-24h,to:now))&_a=(description:'',filters:!(),fullScreenMode:!f,options:(hidePanelTitles:!f,useMargins:!t),panels:!((embeddableConfig:(),gridData:(h:15,i:'32cd7a00-efe5-47c0-bd18-ac6efea2487c',w:24,x:0,y:0),id:a4ce1120-a9e3-11ea-8d10-c7a5eafb804a,panelIndex:'32cd7a00-efe5-47c0-bd18-ac6efea2487c',type:visualization,version:'7.5.2'),(embeddableConfig:(),gridData:(h:15,i:'4690bc36-b76e-404a-91c0-682937e74359',w:24,x:24,y:0),id:'3fc8df90-a9e7-11ea-8d10-c7a5eafb804a',panelIndex:'4690bc36-b76e-404a-91c0-682937e74359',type:visualization,version:'7.5.2'),(embeddableConfig:(),gridData:(h:15,i:'213bd924-8edb-40b5-b4b7-4e0263e31411',w:24,x:0,y:15),id:'6a645040-a9e7-11ea-8d10-c7a5eafb804a',panelIndex:'213bd924-8edb-40b5-b4b7-4e0263e31411',type:visualization,version:'7.5.2'),(embeddableConfig:(),gridData:(h:15,i:c017d682-c438-44a0-940a-9e11a5b1a13b,w:24,x:24,y:15),id:e00421b0-a9ea-11ea-8d10-c7a5eafb804a,panelIndex:c017d682-c438-44a0-940a-9e11a5b1a13b,type:visualization,version:'7.5.2'),(embeddableConfig:(),gridData:(h:15,i:'3e82dcfd-0065-4332-8940-38e4a614b3d5',w:24,x:0,y:30),id:'0cae4e20-a9eb-11ea-8d10-c7a5eafb804a',panelIndex:'3e82dcfd-0065-4332-8940-38e4a614b3d5',type:visualization,version:'7.5.2'),(embeddableConfig:(),gridData:(h:15,i:'90bbde13-e6d9-4c95-bcc5-7019728aa4ad',w:24,x:24,y:30),id:'641accd0-a9ee-11ea-8d10-c7a5eafb804a',panelIndex:'90bbde13-e6d9-4c95-bcc5-7019728aa4ad',type:visualization,version:'7.5.2'),(embeddableConfig:(),gridData:(h:15,i:'477003ba-134f-4be9-b59e-c1154b0ab265',w:24,x:0,y:45),id:ac0a2cc0-a9ee-11ea-8d10-c7a5eafb804a,panelIndex:'477003ba-134f-4be9-b59e-c1154b0ab265',type:visualization,version:'7.5.2')),query:(language:kuery,query:''),timeRestore:!f,title:'mon%20troisieme%20tableau',viewMode:view)




Il est aussi possible d'observer les tableau une par une à partir des liens suivantes:
    1-Cours_du_bitcoin_en_euro:
                http://localhost:5601/app/kibana#/visualize/edit/a4ce1120-a9e3-11ea-8d10-c7a5eafb804a?_g=()&_a=(filters:!(),linked:!f,query:(language:kuery,query:''),uiState:(),vis:(aggs:!((enabled:!t,id:'1',params:(customLabel:'Prix%20du%20bitcoin%20en%20euro',field:euro),schema:metric,type:avg),(enabled:!t,id:'2',params:(customLabel:'temps%20t',drop_partials:!f,extended_bounds:(),field:now,interval:'1m',min_doc_count:1,scaleMetricValues:!f,timeRange:(from:now-24h,to:now),useNormalizedEsInterval:!t),schema:segment,type:date_histogram)),params:(addLegend:!t,addTimeMarker:!f,addTooltip:!t,categoryAxes:!((id:CategoryAxis-1,labels:(filter:!f,rotate:75,show:!t,truncate:500),position:bottom,scale:(type:linear),show:!t,style:(),title:(),type:category)),dimensions:(x:(accessor:0,aggType:date_histogram,format:(id:date,params:(pattern:'HH:mm')),params:(bounds:(max:'2020-06-12T21:07:15.490Z',min:'2020-06-11T21:07:15.490Z'),date:!t,format:'HH:mm',interval:PT10M,intervalESUnit:m,intervalESValue:10)),y:!((accessor:1,aggType:avg,format:(id:number),params:()))),grid:(categoryLines:!f),labels:(),legendPosition:top,seriesParams:!((data:(id:'1',label:'Prix%20du%20bitcoin%20en%20euro'),drawLinesBetweenPoints:!t,interpolate:linear,lineWidth:2,mode:normal,show:!t,showCircles:!f,type:line,valueAxis:ValueAxis-1)),thresholdLine:(color:%2334130C,show:!f,style:full,value:10,width:1),times:!(),type:line,valueAxes:!((id:ValueAxis-1,labels:(filter:!f,rotate:0,show:!t,truncate:100),name:LeftAxis-1,position:left,scale:(defaultYExtents:!f,mode:silhouette,setYExtents:!t,type:linear),show:!t,style:(),title:(text:'Prix%20du%20bitcoin%20en%20euro'),type:value))),title:'1-cours%20du%20bitcoin%20en%20euro',type:line))


    2-a)Volume_bitcoin_échangés_par_heure_(en_BTC):
                http://localhost:5601/app/kibana#/visualize/edit/3fc8df90-a9e7-11ea-8d10-c7a5eafb804a?_g=()&_a=(filters:!(),linked:!f,query:(language:kuery,query:''),uiState:(),vis:(aggs:!((enabled:!t,id:'1',params:(customLabel:'Volume%20%C3%A9chang%C3%A9%20(BTC)',field:montantTotalTransactionBitcoin),schema:metric,type:sum),(enabled:!t,id:'2',params:(customLabel:'temps%20h',drop_partials:!f,extended_bounds:(),field:untilDate,interval:h,min_doc_count:1,scaleMetricValues:!f,timeRange:(from:now-24h,to:now),useNormalizedEsInterval:!t),schema:segment,type:date_histogram)),params:(addLegend:!t,addTimeMarker:!f,addTooltip:!t,categoryAxes:!((id:CategoryAxis-1,labels:(filter:!f,rotate:75,show:!t,truncate:100),position:bottom,scale:(type:linear),show:!t,style:(),title:(),type:category)),dimensions:(x:(accessor:0,aggType:date_histogram,format:(id:date,params:(pattern:'YYYY-MM-DD%20HH:mm')),params:(bounds:(max:'2020-06-09T01:34:03.490Z',min:'2020-06-08T01:34:03.490Z'),date:!t,format:'YYYY-MM-DD%20HH:mm',interval:PT1H,intervalESUnit:h,intervalESValue:1)),y:!((accessor:1,aggType:sum,format:(id:number),params:()))),grid:(categoryLines:!f),labels:(show:!f),legendPosition:top,seriesParams:!((data:(id:'1',label:'Volume%20%C3%A9chang%C3%A9%20(BTC)'),drawLinesBetweenPoints:!t,lineWidth:2,mode:stacked,show:!t,showCircles:!t,type:histogram,valueAxis:ValueAxis-1)),thresholdLine:(color:%2334130C,show:!f,style:full,value:10,width:1),times:!(),type:histogram,valueAxes:!((id:ValueAxis-1,labels:(filter:!f,rotate:0,show:!t,truncate:100),name:LeftAxis-1,position:left,scale:(mode:normal,setYExtents:!f,type:linear),show:!t,style:(),title:(text:'Volume%20%C3%A9chang%C3%A9%20(BTC)'),type:value))),title:'2-a)%20volume%20%C3%A9chang%C3%A9e%20par%20heure%20en%20bitcoin',type:histogram))


    3-a)Valeur_maximale_des_transactions_réalisées_par_heure_(en_BTC):
                http://localhost:5601/app/kibana#/visualize/edit/e00421b0-a9ea-11ea-8d10-c7a5eafb804a?_g=()&_a=(filters:!(),linked:!f,query:(language:kuery,query:''),uiState:(),vis:(aggs:!((enabled:!t,id:'1',params:(customLabel:'Maximal%20des%20transactions%20(BTC)',field:transactionMaxBitcoin),schema:metric,type:max),(enabled:!t,id:'2',params:(customLabel:'Temps%20heure',drop_partials:!f,extended_bounds:(),field:untilDate,interval:h,min_doc_count:1,scaleMetricValues:!f,timeRange:(from:now-24h,to:now),useNormalizedEsInterval:!t),schema:segment,type:date_histogram)),params:(addLegend:!t,addTimeMarker:!f,addTooltip:!t,categoryAxes:!((id:CategoryAxis-1,labels:(filter:!t,rotate:75,show:!t,truncate:100),position:bottom,scale:(type:linear),show:!t,style:(),title:(),type:category)),dimensions:(x:(accessor:0,aggType:date_histogram,format:(id:date,params:(pattern:'YYYY-MM-DD%20HH:mm')),params:(bounds:(max:'2020-06-09T01:36:17.358Z',min:'2020-06-08T01:36:17.358Z'),date:!t,format:'YYYY-MM-DD%20HH:mm',interval:PT1H,intervalESUnit:h,intervalESValue:1)),y:!((accessor:1,aggType:max,format:(id:number),params:()))),grid:(categoryLines:!f),labels:(show:!f),legendPosition:top,seriesParams:!((data:(id:'1',label:'Maximal%20des%20transactions%20(BTC)'),drawLinesBetweenPoints:!t,lineWidth:2,mode:stacked,show:!t,showCircles:!t,type:histogram,valueAxis:ValueAxis-1)),thresholdLine:(color:%2334130C,show:!f,style:full,value:10,width:1),times:!(),type:histogram,valueAxes:!((id:ValueAxis-1,labels:(filter:!f,rotate:0,show:!t,truncate:100),name:LeftAxis-1,position:left,scale:(defaultYExtents:!f,mode:normal,setYExtents:!f,type:linear),show:!t,style:(),title:(text:'Maximal%20des%20transactions%20(BTC)'),type:value))),title:'3-a)%20valeur%20maximale%20des%20transactions%20r%C3%A9alis%C3%A9es%20%2F%20heure%20(en%20BTC)',type:histogram))
