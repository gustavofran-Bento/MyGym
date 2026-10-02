# MyGym

App Android feito em Kotlin para a Parcial (Android Views com XML, navegação e Intent).

## Sobre o app

A ideia do MyGym é juntar em um app só o treino e a alimentação. No cadastro a pessoa coloca peso, altura, idade e objetivo, e o app calcula a meta de calorias e de proteína, carboidrato e gordura do dia. Depois é só ir registrando o que comeu em cada refeição e os treinos feitos.

O app tem 3 telas:

1. **Login** – entrar ou criar conta (a meta diária aparece enquanto você preenche o cadastro).
2. **Diário** – tela principal. Tem o filtro da semana com um gráfico, o resumo de calorias e macros do dia, água, as refeições (café, almoço, lanche e jantar), o botão de finalizar o dia e a lista de treinos.
3. **Detalhe do treino** – abre ao tocar em um treino. Dá para marcar os exercícios feitos, adicionar exercício e finalizar o treino.

Adicionar alimento, cadastrar alimento novo e criar treino abrem em janelas dentro dessas telas.

## Conta de teste

Na primeira vez que o app abre ele cria uma conta com alguns dias de exemplo:

- E-mail: `gustavo@mygym.com`
- Senha: `mygym123`

Na tela de login tem o botão "Preencher com a conta demo". Também dá para criar uma conta nova.

## Como rodar

1. Clonar o repositório:
   ```bash
   git clone https://github.com/gustavofran-Bento/MyGym.git
   ```
2. Abrir a pasta `MyGym` no Android Studio (**File → Open**).
3. Esperar o Gradle Sync terminar. Na primeira vez demora alguns minutos porque ele baixa as dependências e, se precisar, o Java 21 que o projeto usa para compilar.
4. Se o Android Studio pedir para instalar o **Android SDK 34**, aceitar.
5. Escolher um emulador (Android 8.0 ou mais novo) e clicar em **Run ▶**.

O projeto foi configurado com versões que abrem tanto em Android Studios mais antigos (a partir do Hedgehog, 2023) quanto nos mais novos. Precisa de internet na primeira vez.

Também dá para compilar pelo terminal:
```bash
./gradlew assembleDebug        # gera o APK
./gradlew testDebugUnitTest    # roda os testes
```

## Bibliotecas usadas

| Biblioteca | Para que usei |
|---|---|
| androidx.core (core-ktx) | Funções de apoio do Kotlin para Android |
| androidx.appcompat | Base das Activities (`AppCompatActivity`) |
| Material Components | Cards, botões, campos de texto, barras de progresso, janelas e o painel de baixo |
| androidx.recyclerview | Listas de alimentos, treinos e exercícios |
| androidx.fragment | Fragment do resumo do dia |
| JUnit | Testes |

Não usei nenhuma API externa nem chave/senha de serviço.

## Como o código está organizado

```
app/src/main/java/com/gustavobento/mygym/
├── model/       data classes (User, Food, Macros, DiaryEntry, Workout...)
├── data/        onde os dados são salvos e os dados de exemplo (mocks)
├── di/          AppContainer, que cria os repositórios
├── util/        cálculos (metas, macros, IMC), validações e hash da senha
└── ui/          as telas: auth (Tela 1), diary (Tela 2), workout (Tela 3) e food
```

Os layouts ficam em `app/src/main/res/layout`.

Algumas decisões:
- Usei **ViewBinding** em todas as telas (não tem `findViewById`).
- A navegação da Tela 2 para a Tela 3 é por **Intent explícita**, passando o id e o nome do treino.
- O resumo do dia é um **Fragment**.
- Os dados de exemplo (alimentos da Tabela TACO, treinos e conta demo) ficam em `data/mock`. O que o usuário registra é salvo no próprio celular com SharedPreferences, para não perder ao fechar o app.
- A senha não é salva: guardo só um hash dela.
- As metas são calculadas pela fórmula de Mifflin-St Jeor. As calorias de um alimento novo são proteína × 4 + carboidrato × 4 + gordura × 9.

## Testes

Tem 35 testes de unidade em `app/src/test` (cálculo das metas, validações, senha, finalizar o dia etc.).

## Autor

Gustavo Francisco Bento
