This holds the application used as CM Entry point for Access the S-RPF Services (S-RPF Front End).


aggiunta variabile di configurazione minTimeBetweenAcqAndRelatedDwl che esprime in millisecondi la durata minima che deve intercorrere tra una acquisizione ed il relativo download
aggiunta variabile di configurazione antennaRightLookSideAvailability che, se settata ad 1 indica che l'assetto right non è disponibile per il satellite. Se sono previsti download questi dovranno essere scaricati con assetto left dopo aver pianificato una manovra per cambiare l'assetto
aggiunta variabile di configurazione antennaLeftLookSideAvailability che, se settata ad 1 indica che l'assetto left non è disponibile per il satellite. Se sono previsti download questi dovranno essere scaricati con assetto right dopo aver pianificato una manovra per cambiare l'assetto
aggiunto parametro di configurazione timeEnableDisableCarrier che esprime (in millisecondi) il tempo necessario per attivare/disattivare la portante per gli scarichi
