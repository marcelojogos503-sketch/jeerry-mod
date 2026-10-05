# Shadow Hound - Minecraft Forge Mod (1.20.1)

Mod de um cão sombrio que muda de comportamento conforme os dias passam no mundo.

## Fases do Hound
- **0** (dias 0-1): Amigável, segue o jogador
- **1** (dias 2-4): Observa de longe e some se você chega perto
- **2** (dias 5-7): Estilo Weeping Angel – só se move quando você não está olhando
- **3** (dia 8+): Agressivo, causa efeito de Escuridão

## Comandos
- `/shadowhound stage < -1 a 3 >` — força a fase (-1 = automático)
- `/shadowhound info` — mostra a fase forçada atual

## Comandos de chat para lobos domesticados
Fale no chat (ou use mod de voz):
- "senta", "vem", "rola", "pula", "gira", "busca", "bom garoto"
- Pode usar o nome do lobo: "Jerry, senta"

## Como gerar o .jar

### Requisitos
- JDK 17
- Internet (na primeira vez o Gradle baixa o Minecraft + Forge)

### Passos
```bash
# No Windows (PowerShell ou CMD)
gradlew.bat build

# No Linux / Mac
./gradlew build
```

O arquivo final fica em:
```
build/libs/shadowhound-1.0.0.jar
```

Coloque esse jar na pasta `mods` do Minecraft com Forge 1.20.1.

## Estrutura
```
src/main/java/com/shadowhound/
  ShadowHoundMod.java   ← classe principal
  ModEntities.java      ← registro da entidade
  HoundEntity.java      ← lógica do cão
  ClientSetup.java      ← renderização
  WolfCommands.java     ← comandos de chat
  ModCommands.java      ← /shadowhound
```

## Nota
Este projeto foi montado a partir dos arquivos fonte incompletos que você enviou.
Se encontrar erros de compilação, abra no IntelliJ IDEA (File → Open na pasta do projeto) e deixe o Gradle sincronizar.
