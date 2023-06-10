.PHONY: tm qb

tm:
	cd tm && make
	
tm-run:
	cd tm && make run

qb-run:
	cd qb && python3 main.py
