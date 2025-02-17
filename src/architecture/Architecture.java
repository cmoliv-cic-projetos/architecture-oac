package architecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


import components.Bus;
import components.Demux;
import components.Memory;
import components.Register;
import components.Ula;

public class Architecture {
	
	private boolean simulation; //this boolean indicates if the execution is done in simulation mode.
								//simulation mode shows the components' status after each instruction
	
	
	private boolean halt;
	private Bus extbus1;
	private Bus intbus1;
	private Bus intbus2;
	private Memory memory;
	private Memory statusMemory;
	private int memorySize;
	private Register PC;
	private Register IR;
	private Register RPG;
	private Register RPG1;
	private Register RPG2;
	private Register RPG3;
	private Register Flags;
	private Register StackTop;
	private Register StackBottom;
	private Ula ula;
	private Demux demux; //only for multiple register purposes
	
	private ArrayList<String> commandsList;
	private ArrayList<Register> registersList;
	
	

	/**
	 * Instanciates all components in this architecture
	 */
	private void componentsInstances() {
		//don't forget the instantiation order
		//buses -> registers -> ula -> memory
		extbus1 = new Bus();
		intbus1 = new Bus();
		intbus2 = new Bus();
		PC = new Register("PC", extbus1, intbus2);
		IR = new Register("IR", extbus1, extbus1);
		RPG = new Register("RPG0", extbus1, intbus1);
		RPG1 = new Register ("RPG1", extbus1, intbus1);
		RPG2 = new Register ("RPG2", extbus1, intbus1);
		RPG3 = new Register ("RPG3", extbus1, intbus1);
		Flags = new Register(2, intbus2);
		StackTop = new Register("StackTop", intbus2, intbus2);
		StackBottom = new Register("StackBottom", intbus2, intbus2);
		fillRegistersList();
		ula = new Ula(intbus1, intbus2);
		statusMemory = new Memory(2, extbus1);
		memorySize = 128;
		memory = new Memory(memorySize, extbus1);
		demux = new Demux(); //this bus is used only for multiple register operations
		
		fillCommandsList();
	}

	/**
	 * This method fills the registers list inserting into them all the registers we have.
	 * IMPORTANT!
	 * The first register to be inserted must be the default RPG
	 */
	private void fillRegistersList() {
		registersList = new ArrayList<Register>();
		registersList.add(RPG);
		registersList.add(RPG1);
		registersList.add(RPG2);
		registersList.add(RPG3);
		registersList.add(PC);
		registersList.add(IR);
		registersList.add(Flags);
		registersList.add(StackBottom);
		registersList.add(StackTop);
	}

	/**
	 * Constructor that instanciates all components according the architecture diagram
	 */
	public Architecture() {
		componentsInstances();
		
		//by default, the execution method is never simulation mode
		simulation = false;
	}

	
	public Architecture(boolean sim) {
		componentsInstances();
		
		//in this constructor we can set the simoualtion mode on or off
		simulation = sim;
	}



	//getters
	
	protected Bus getExtbus1() {
		return extbus1;
	}

	protected Bus getIntbus1() {
		return intbus1;
	}

	protected Bus getIntbus2() {
		return intbus2;
	}

	protected Memory getMemory() {
		return memory;
	}

	protected Register getPC() {
		return PC;
	}

	protected Register getIR() {
		return IR;
	}

	protected Register getRPG() {
		return RPG;
	}

	protected Register getRPG1() {
		return RPG1;
	}

	protected Register getRPG2() {
		return RPG2;
	}

	protected Register getRPG3() {
		return RPG3;
	}

	protected Register getFlags() {
		return Flags;
	}

	protected Register getStackTop() {
		return StackTop;
	}

	protected Register getStackBottom() {
		return StackBottom;
	}

	protected Ula getUla() {
		return ula;
	}

	public ArrayList<String> getCommandsList() {
		return commandsList;
	}



	// all the microprograms must be implemented here

	/**
	 * This method fills the commands list arraylist with all commands used in this architecture
	 */
	protected void fillCommandsList() {
		commandsList = new ArrayList<String>();

		commandsList.add("addRegReg");   // 0
		commandsList.add("addMemReg");   // 1
		commandsList.add("addRegMem");   // 2
		commandsList.add("addImmReg");   // 3  -

		commandsList.add("subRegReg");   // 4
		commandsList.add("subMemReg");   // 5
		commandsList.add("subRegMem");   // 6
		commandsList.add("subImmReg");   // 7

		commandsList.add("moveMemReg");  // 8
		commandsList.add("moveRegMem");  // 9
		commandsList.add("moveRegReg");  // 10
		commandsList.add("moveImmReg");  // 11

		commandsList.add("inc");         // 12

		commandsList.add("jmp");         // 13
		commandsList.add("jz");          // 14
		commandsList.add("jn");          // 15

		commandsList.add("jeq");         // 16
		commandsList.add("jneq");        // 17
		commandsList.add("jgt");         // 18
		commandsList.add("jlw");         // 19

		commandsList.add("call");        // 20
		commandsList.add("ret");         // 21
	}

	// Add commands
	protected void addRegReg() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Ula(0) <- RegA
		PC.read();
		memory.read();                  // o segundo registrador
		demux.setValue(extbus1.get());  // aponta para o registrador correto
		registersInternalRead();        // começa a leitura do registrador selecionado
		ula.store(0);				// armazena o valor do external bus no RegA (RPG0)

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Ula(1) <- RegB
		// Pega o ID do registrador e joga seu valor na ULA
		PC.read();
		memory.read();                  // o segundo registrador
		demux.setValue(extbus1.get());  // aponta para o registrador correto
		registersInternalRead();        // começa a leitura do registrador
		ula.store(1);				// armazena o valor do external bus no RegB (RPG1)

		ula.add();						// ula soma e escreve em RegB (RPG1)

		// RegB <- Resultado da soma (Ula+)
		ula.internalRead(1);		 // lê o valor da ula e guarda no internal bus
		setStatusFlags(intbus2.get());	 // verifica o resultado - muda o status flag a depender do valor
		ula.read(1);				 // lê o valor da ula e guarda no external bus
		demux.setValue(extbus1.get());   // aponta para o registrador correto
		registersInternalStore();		 // começa a leitura do registrador

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	protected void addMemReg() {
		// PC++
		PC.internalRead();						// copia os dados do PC para o internal bus
		ula.internalStore(1);				// pega o valor que tá em bus e guarda na ula
		ula.inc();								// ula incrementa
		ula.internalRead(1);				// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();						// copia os dados do internal bus e armazena em PC

		// StackTop <- PC : Guarda o dado ao qual PC aponta para uso posterior
		PC.internalRead();						// copia o dado do PC para o internal bus

		// Mem[StackTop] = dataBus
		int position = StackTop.getData();		// guarda a posição da memória ao qual PC apontava
		int data = intbus2.get();				// guarda o dado de PC que estava em internal bus

		memory.getDataList()[position] = data;	// memoiza o dado que estava no internal bus na memória[StackTop]

		// StackTop aponta para uma posição de memória acima
		intbus2.put(position - 1);
		StackTop.store();

		// Substitui o dado que está no internal bus
		intbus2.put(data);

		// Ula(0) <- Mem
		PC.read();					// pega o dado de PC e armazena em external bus
		memory.read();     			// o segundo registrador
		memory.read();
		PC.store();					// armazena o endereço salvo no external bus
		PC.internalRead();			// copia o endereço de PC para o internal bus
		ula.internalStore(0);	// copia o dado do internal bus para o Reg0

		// PC <- StackTop
		// Tenta acessar a posição abaixo do stack top
		position = StackTop.getData() + 1;

		// Recupera dado salvo
		data = memory.getDataList()[position];

		intbus2.put(position);
		StackTop.store();

		// Remove o dado da memória
		memory.getDataList()[position] = 0;

		// internal bus pega o dado e PC lê em seguida
		intbus2.put(data);
		PC.internalStore();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Ula(1) <- RegA
		// Pega o ID do registrador e joga seu valor na ULA
		PC.read();
		memory.read();                  // o segundo registrador
		demux.setValue(extbus1.get());  // aponta para o registrador correto
		registersInternalRead();        // começa a leitura do registrador
		ula.store(1);				// copia o dado do external bus para o Reg1

		ula.add();						// ula soma e escreve em RPG1

		// RegA <- Ula+
		ula.internalRead(1);		// lê valor do RPG1
		setStatusFlags(intbus2.get());	// verifica valor retornada da soma - muda o status flag a depender do valor
		ula.read(1);				// guarda o resultado no external bus
		demux.setValue(extbus1.get());  // aponta para o registrador correto
		registersInternalStore();		// escreve no registrador

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	protected void addRegMem() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Pega o valor do registrador e guarda na ULA
		PC.read();
		memory.read();
		demux.setValue(extbus1.get());
		registersInternalRead();
		ula.store(0);

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Guarda o valor de PC no stack top
		PC.internalRead();

		// Mem[StackTop] = dataBus
		int position = StackTop.getData();
		int data = intbus2.get();

		memory.getDataList()[position] = data;

		// StackTop aponta para uma posição acima
		intbus2.put(position - 1);
		StackTop.store();

		// Substituindo o dado do internal bus
		intbus2.put(data);

		// Pega o valor da memória, passa pelo PC e joga na ULA (RPG1)
		PC.read();
		memory.read();
		memory.read();
		PC.store();
		PC.internalRead();
		ula.internalStore(1);

		// Realiza a soma, passa o valor da soma para o PC e guarda no IR
		ula.add();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		PC.internalStore();
		PC.read();
		IR.store();

		// Devolve o valor do PC e pega o endereço para armazenar o valor da soma
		// Tenta acessar a posição abaixo do StackTop
		position = StackTop.getData() + 1;

		// Dado salvo
		data = memory.getDataList()[position];

		intbus2.put(position);
		StackTop.store();

		// Dado removido da memória
		memory.getDataList()[position] = 0;

		// Internal bus guarda o dado
		intbus2.put(data);
		PC.internalStore();
		PC.read();
		memory.read();
		memory.store();
		IR.read();
		memory.store();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	protected void addImmReg() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Guarda o valor do PC no internal bus
		PC.internalRead();

		// Mem[StackTop] = dataBus
		int position = StackTop.getData();
		int data = intbus2.get();

		memory.getDataList()[position] = data;

		// StackTop aponta para uma posição acima
		intbus2.put(position-1);
		StackTop.store();

		// Substitui o dado no internal bus
		intbus2.put(data);

		// Pega o valor da memória, passa pelo PC e guarda na ULA
		PC.read();
		memory.read();
		PC.store();
		PC.internalRead();
		ula.internalStore(0);

		// Devolve o valor do PC e faz o PC++
		// Tenta acessar a posição abaixo do StackTop
		position = StackTop.getData() + 1;

		// Recupera dado salvo
		data = memory.getDataList()[position];

		intbus2.put(position);
		StackTop.store();

		// Remove dado da memória
		memory.getDataList()[position] = 0;

		// Internal bus guarda o dado, PC lê do internal bus
		intbus2.put(data);
		PC.internalStore();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Pega o ID do registrador e joga seu valor na ULA
		PC.read();
		memory.read();
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 		// começa a leitura do registrador
		ula.store(1);

		// Realiza a soma e passa o valor da soma para o registrador
		ula.add();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		ula.read(1);
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalStore(); 		// começa a leitura do registrador

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	// Sub commands
	protected void subRegReg() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Ula(0) <- RegA
		PC.read();
		memory.read();                   // o segundo registrador
		demux.setValue(extbus1.get());   // aponta para o segundo registrador
		registersInternalRead();         // começa a leitura do registrador
		ula.store(0);

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Ula(1) <- RegB
		PC.read();
		memory.read();                  // o segundo registrador
		demux.setValue(extbus1.get());  // aponta para o registrador correto
		registersInternalRead();        // começa a leitura do registrador
		ula.store(1);

		ula.sub();						// realiza a subtração e armazena o valor registrador

		// RegB <- Ula-
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		ula.read(1);
		demux.setValue(extbus1.get());  // aponta para o registrador correto
		registersInternalStore();		// escreve no registrador

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	protected void subMemReg() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// StackTop <- PC
		PC.internalRead();

		// Mem[StackTop] = dataBus
		int position = StackTop.getData();
		int data = intbus2.get();

		memory.getDataList()[position] = data;

		// StackTop aponta para uma posição acima
		intbus2.put(position - 1);
		StackTop.store();

		// Substitui o dado no internal bus
		intbus2.put(data);

		// Ula(0) <- Mem
		PC.read();					// pega o dado de PC e armazena em external bus
		memory.read();     			// o segundo registrador
		memory.read();
		PC.store();					// armazena o endereço salvo no external bus
		PC.internalRead();			// copia o endereço de PC para o internal bus
		ula.internalStore(0);	// copia o dado do internal bus para o Reg0

		// PC <- StackTop
		// Tenta acessar a posição abaixo do StackTop
		position = StackTop.getData() + 1;

		// Recupear o dado salvo
		data = memory.getDataList()[position];

		intbus2.put(position);
		StackTop.store();

		// Remove dado da memória
		memory.getDataList()[position] = 0;

		// internal bus salva o dado
		intbus2.put(data);
		PC.internalStore();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Ula(1) <- RegA
		PC.read();
		memory.read();                  // o segundo registrador
		demux.setValue(extbus1.get());  // aponta para o registrador correto
		registersInternalRead();        // inicia a leitura do registrador
		ula.store(1);

		ula.sub();						// realiza a operação de subtração

		// RegB <- Ula-
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		ula.read(1);
		demux.setValue(extbus1.get());  // aponta para o registrador correto
		registersInternalStore();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	protected void subRegMem() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Pega o valor do registrador correto e guarda na ULA
		PC.read();
		memory.read();
		demux.setValue(extbus1.get());
		registersInternalRead();
		ula.store(0);

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Guarda o valor do PC
		PC.internalRead();

		// Mem[StackTop] = dataBus
		int position = StackTop.getData();
		int data = intbus2.get();

		memory.getDataList()[position] = data;

		// StackTop aponta para uma posição acima
		intbus2.put(position - 1);
		StackTop.store();

		// Substitui o dado no internal bus
		intbus2.put(data);

		// Pega o valor da memória, passa pelo PC e guarda na ULA
		PC.read();
		memory.read();
		memory.read();
		PC.store();
		PC.internalRead();
		ula.internalStore(1);

		// Realiza a subtração, passa o valor da soma pelo pc e guarda no IR
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		PC.internalStore();
		PC.read();
		IR.store();

		// Devolve o valor do PC e pega o endereço para armazenar o valor da soma
		// Tenta acessar a posição abaixo do StackTop
		position = StackTop.getData() + 1;

		// Dado salvo
		data = memory.getDataList()[position];

		intbus2.put(position);
		StackTop.store();

		// Remove dado da memória
		memory.getDataList()[position] = 0;

		// Internal busa guarda o dado
		intbus2.put(data);
		PC.internalStore();
		PC.read();
		memory.read();
		memory.store();
		IR.read();
		memory.store();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	protected void subImmReg() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Guarda o valor do PC no internal bus
		PC.internalRead();

		// Mem[StackTop] = dataBus
		int position = StackTop.getData();
		int data = intbus2.get();

		memory.getDataList()[position] = data;

		// StackTop aponta para uma posição acima
		intbus2.put(position-1);
		StackTop.store();

		// Replacing the data on the bus
		intbus2.put(data);

		//Pegar o valor da memória, passar pelo PC, e meter na ULA
		PC.read();
		memory.read();
		PC.store();
		PC.internalRead();
		ula.internalStore(0);

		//Devolver o valor do PC e fazer o PC ++
		// Try to access the position below the stack top
		position = StackTop.getData() + 1;

		// Saved data
		data = memory.getDataList()[position];

		intbus2.put(position);
		StackTop.store();

		// Data removed from memory
		memory.getDataList()[position] = 0;

		// Bus get the data
		intbus2.put(data);
		PC.internalStore();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		//Pegar o ID do registrador e jogar seu valor na ULA
		PC.read();
		memory.read();
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 	   	// começa a leitura do registrador
		ula.store(1);

		//Fazer o sub, e passar o valor da soma para o registrador
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());
		ula.read(1);
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalStore(); 		// escreve no registrador

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	// Move commands
	public void moveMemReg() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Guarda o valor de PC no StackTop
		PC.internalRead();

		// Mem[StackTop] = dataBus
		int position = StackTop.getData();
		int data = intbus2.get();

		memory.getDataList()[position] = data;

		// StackTop points to a position above
		intbus2.put(position-1);
		StackTop.store();

		// Substitui o dado no internal bus
		intbus2.put(data);

		// Pega o valor da memoria e joga na ula, atravessando o rio nilo e o PC
		PC.read();
		memory.read();
		memory.read();
		PC.store();
		PC.internalRead();
		ula.internalStore(0);

		// Resgata o valor do PC
		// Tenta acessar a posição abaixo do StackTop
		position = StackTop.getData() + 1;

		// Recupera o dado salvo
		data = memory.getDataList()[position];

		intbus2.put(position);
		StackTop.store();

		// Remove o dado da memória
		memory.getDataList()[position] = 0;

		// Internal bus salvo o dado
		intbus2.put(data);
		PC.internalStore();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Joga o valor da memória no registrador
		ula.read(0);
		PC.read();
		memory.read();
		demux.setValue(extbus1.get());
		registersInternalStore();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	public void moveRegMem() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Pega o id do registrador e aguarda o comando
		PC.read();
		memory.read();
		demux.setValue(extbus1.get());

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Pega o endereço de memória e dá o comando pro registrador
		// Cospe o dado, guardando na memória
		PC.read();
		memory.read();
		memory.store();
		registersRead();
		memory.store();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	public void moveRegReg() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();						// armazena endereço que PC aponta no external bus
		memory.read();					// armazena valor da memória no external bus
		demux.setValue(extbus1.get());	// aponta para o registrador correto
		registersInternalRead();		// inicia a leitura do registrador
		ula.store(0);

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();						// armazena endereço que PC aponta no external bus
		memory.read();					// armazena valor da memória no external bus
		demux.setValue(extbus1.get());	// aponta para o registrador correto
		ula.read(0);				// lê dado armazenado no RPG0 (RegA)
		registersInternalStore();		// inicia a escrita no RPG1 (RegB)

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	public void moveImmReg() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read();
		IR.store();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read();
		demux.setValue(extbus1.get());
		IR.read();
		registersStore();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	// Increment command
	public void inc() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// Ula(1) <- RegA
		PC.read();
		memory.read();
		demux.setValue(extbus1.get());   // aponta para o registrador correto
		registersInternalRead();
		ula.store(1);

		ula.inc();

		// RegA <- Ula++
		ula.read(1);
		setStatusFlags(intbus1.get());
		demux.setValue(extbus1.get());   // aponta para o registrador correto
		registersInternalStore();

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC
	}

	// Deviations
	public void jmp() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// PC <- Mem
		PC.read();						// lê o dado do PC e guarda no external bus
		memory.read();					// copia o dado da memória e guarda no external bus
		PC.store();						// guarda o valor que está no external bus no PC
	}

	public void jz(){
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		if (Flags.getBit(0) == 1){
			PC.read();
			memory.read();
			PC.store();
		}
		else {
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC
		}
	}

	public void jn(){
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		if (Flags.getBit(1) == 1){
			PC.read();
			memory.read();
			PC.store();
		}
		else{
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC
		}
	}

	public void jeq(){
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read(); 					// o segundo registrador
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 		// começa a aleitura do registrador
		ula.store(0);

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read(); 					// o segundo registrador
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 		// começa a leitura do registrador

		ula.store(1);
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());

		if (Flags.getBit(0)==1){
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC

			PC.read();
			memory.read();
			PC.store();
		}
		else {
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC

			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC
		}
	}

	public void jneq(){
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read(); 					// o segundo registrador
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 		// começa a leitura do registrador
		ula.store(0);

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read(); 					// o segundo registrador
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 		// começa a leitura do registrador

		ula.store(1);
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());

		if (Flags.getBit(0) != 1){
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC

			PC.read();
			memory.read();
			PC.store();
		}
		else {
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC

			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC
		}
	}

	public void jgt(){
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read(); 					// o segundo registrador
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 		// começa a leitura do registrador
		ula.store(0);

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read(); 					// o segundo registrador
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 		// começa a leitura do registrador

		ula.store(1);
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());

		if (Flags.getBit(0)==0 && Flags.getBit(1)==0){
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC

			PC.read();
			memory.read();
			PC.store();
		}
		else{
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC

			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC
		}
	}

	public void jlw(){
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read(); 					// o segundo registrador
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 		// começa a leitura do registrador
		ula.store(0);

		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		PC.read();
		memory.read(); 					// o segundo registrador
		demux.setValue(extbus1.get()); 	// aponta para o registrador correto
		registersInternalRead(); 		// começa a leitura do registrador

		ula.store(1);
		ula.sub();
		ula.internalRead(1);
		setStatusFlags(intbus2.get());

		if (Flags.getBit(1)==1){
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC

			// jump
			PC.read();
			memory.read();
			PC.store();
		}
		else{
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC

			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC
		}
	}

	public void call() {
		// PC++
		PC.internalRead();				// copia os dados do PC para o internal bus
		ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
		ula.inc();						// ula incrementa
		ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
		PC.internalStore();				// copia os dados do internal bus e armazena em PC

		// StackTop <- PC+1
		ula.inc();
		ula.internalRead(1);

		// Mem[StackTop] = dataBus
		int position = StackTop.getData();
		int data = intbus2.get();

		memory.getDataList()[position] = data;

		// StackTop aponta para uma posição acima
		intbus2.put(position-1);
		StackTop.store();

		// Substitui o dado no internal bus
		intbus2.put(data);

		// PC <- Mem (jump)
		PC.read();
		memory.read();
		PC.store();
	}

	public void ret() {
		// PC <- StackTop
		if(StackTop.getData() == StackBottom.getData()){
			// PC++
			PC.internalRead();				// copia os dados do PC para o internal bus
			ula.internalStore(1);		// pega o valor que tá em bus e guarda na ula
			ula.inc();						// ula incrementa
			ula.internalRead(1);		// lê o valor que está na ula e guarda no internal bus
			PC.internalStore();				// copia os dados do internal bus e armazena em PC
		}
		else{
			// Tenta acessar a posição abaixo do StackTop
			int position = StackTop.getData() + 1;

			// Recupera dado salvo
			int data = memory.getDataList()[position];

			intbus2.put(position);
			StackTop.store();

			// Remove dado da memória
			memory.getDataList()[position] = 0;

			// Internal bus salvo o dado
			intbus2.put(data);
			PC.internalStore();
		}
	}


	/**
	 * This method is used after some ULA operations, setting the flags bits according the result.
	 * @param result is the result of the operation
	 * NOT TESTED!!!!!!!
	 */
	private void setStatusFlags(int result) {
		Flags.setBit(0, 0);
		Flags.setBit(1, 0);
		if (result==0) { //bit 0 in flags must be 1 in this case
			Flags.setBit(0,1);
		}
		if (result<0) { //bit 1 in flags must be 1 in this case
			Flags.setBit(1,1);
		}
	}

	public ArrayList<Register> getRegistersList() {
		return registersList;
	}

	/**
	 * This method performs an (external) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersRead() {
		registersList.get(demux.getValue()).read();
	}

	/**
	 * This method performs an (internal) read from a register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalRead() {
		registersList.get(demux.getValue()).internalRead();
	}

	/**
	 * This method performs an (external) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersStore() {
		registersList.get(demux.getValue()).store();
	}

	/**
	 * This method performs an (internal) store toa register into the register list.
	 * The register id must be in the demux bus
	 */
	private void registersInternalStore() {
		registersList.get(demux.getValue()).internalStore();
	}



	/**
	 * This method reads an entire file in machine code and
	 * stores it into the memory
	 * NOT TESTED
	 * @param filename
	 * @throws IOException
	 */
	public void readExec(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new
				FileReader(filename+".dxf"));
		String linha;
		int i=0, ok=0;
		int position_Stack;

		while((linha = br.readLine()) != null) {
			if (ok!=-1) {
				extbus1.put(i);
				memory.store();
				ok = Integer.parseInt(linha);
				extbus1.put(ok);
				memory.store();
				i++;
			}
			else {
				// Guarda a posição da Stack e a inicializa
				position_Stack = Integer.parseInt(linha);
				initializeStack(position_Stack);
			}
		}

		br.close();
	}

	protected void initializeStack(int position) {
		intbus2.put(position);
		StackTop.store();
		StackBottom.store();
	}

	/**
	 * This method executes a program that is stored in the memory
	 */
	public void controlUnitEexec() {
		halt = false;
		while (!halt) {
			fetch();
			decodeExecute();
		}
	}


	/**
	 * This method implements The decode proccess,
	 * that is to find the correct operation do be executed
	 * according the command.
	 * And the execute proccess, that is the execution itself of the command
	 */
	private void decodeExecute() {
		IR.internalRead(); //the instruction is in the internalbus2
		int command = extbus1.get();
		simulationDecodeExecuteBefore(command);

		switch (command) {
			case 0: addRegReg(); break;
			case 1: addMemReg(); break;
			case 2: addRegMem(); break;
			case 3: addImmReg(); break;

			case 4: subRegReg(); break;
			case 5: subMemReg(); break;
			case 6: subRegMem(); break;
			case 7: subImmReg(); break;

			case 8:  moveMemReg(); break;
			case 9:  moveRegMem(); break;
			case 10: moveRegReg(); break;
			case 11: moveImmReg(); break;

			case 12: inc(); break;

			case 13: jmp(); break;
			case 14: jz();  break;
			case 15: jn();  break;

			case 16: jeq();  break;
			case 17: jneq(); break;
			case 18: jgt();  break;
			case 19: jlw();  break;

			case 20: call(); break;
			case 21: ret();  break;

			default: halt = true; break;
		}

		if (simulation)
			simulationDecodeExecuteAfter();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED
	 * @param command
	 */
	private void simulationDecodeExecuteBefore(int command) {
		System.out.println("----------BEFORE Decode and Execute phases--------------");
		String instruction;
		int parameter  = 0;
		int parameter2 = 0;
		int parameter3 = 0;

		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}

		if (command !=-1)
			instruction = commandsList.get(command);
		else
			instruction = "END";

		if (hasOperands(instruction)) {
			parameter = memory.getDataList()[PC.getData()+1];

			if (hasOneOperand(command))
				System.out.println("Instruction: "+instruction+" "+parameter);
			else {
				parameter2 = memory.getDataList()[PC.getData()+2];

				if (hasTwoOperands(command))
					System.out.println("Instruction: "+instruction+" "+parameter+
							" "+parameter2);
				else {
					parameter3 = memory.getDataList()[PC.getData()+3];
					System.out.println("Instruction: "+instruction+" "+parameter+
							" "+parameter2+
							" "+parameter3);
				}
			}
		}
		else
			System.out.println("Instruction: "+instruction);
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED
	 */
	private void simulationDecodeExecuteAfter() {
		String instruction;
		System.out.println("-----------AFTER Decode and Execute phases--------------");
		System.out.println("Internal Bus 1: "+intbus1.get());
		System.out.println("Internal Bus 2: "+intbus2.get());
		System.out.println("External Bus 1: "+extbus1.get());
		for (Register r:registersList) {
			System.out.println(r.getRegisterName()+": "+r.getData());
		}
		Scanner entrada = new Scanner(System.in);
		System.out.println("Press <Enter>");
		String mensagem = entrada.nextLine();
	}

	/**
	 * This method uses PC to find, in the memory,
	 * the command code that must be executed.
	 * This command must be stored in IR
	 * NOT TESTED!
	 */
	private void fetch() {
		PC.read();
		memory.read();
		IR.store();
		simulationFetch();
	}

	/**
	 * This method is used to show the components status in simulation conditions
	 * NOT TESTED!!!!!!!!!
	 */
	private void simulationFetch() {
		if (simulation) {
			System.out.println("-------Fetch Phase------");
			System.out.println("PC: "+PC.getData());
			System.out.println("IR: "+IR.getData());
		}
	}

	/**
	 * This method is used to show in a correct way the operands (if there is any) of instruction,
	 * when in simulation mode
	 * NOT TESTED!!!!!
	 * @param instruction
	 * @return
	 */
	private boolean hasOperands(String instruction) {
		if ("ret".equals(instruction))   //ret is the only one instruction having no operands
			return false;
		else
			return true;
	}

	private boolean hasOneOperand(int command) {
		if (command>=12 && command<=15 ||
				command==20)
			return true;
		else
			return false;
	}

	private boolean hasTwoOperands(int command) {
		if (command>=0 && command<=11)
			return true;
		else
			return false;
	}

	/**
	 * This method returns the amount of positions allowed in the memory
	 * of this architecture
	 * NOT TESTED!!!!!!!
	 * @return
	 */
	public int getMemorySize() {
		return memorySize;
	}

	public static void main(String[] args) throws IOException {
		Architecture arch = new Architecture(true);
		arch.readExec("program");
		arch.controlUnitEexec();
	}
}
