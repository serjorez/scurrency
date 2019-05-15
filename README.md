# scurrency

Tool for tracking the dynamics of real-time cryptocurrency.

## Setting up

To run the application you must have private key from coinmarketcap API.
To get it, please [make yourself a developers account.](https://coinmarketcap.com/api/)
After login you will be able to copy your private key from dashboard.

### Setting up environment variables

```bash
export CMC_SECRET_KEY={your private key from coinmarketcap API}
```

## Running application

```bash
sbt run
```