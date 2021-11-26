package DSCoinPackage;

public class TransactionQueue {

    public Transaction firstTransaction;
    public Transaction lastTransaction;
    public int numTransactions;

    public void AddTransactions(Transaction transaction) {
        if (numTransactions == 0) {
            transaction.previous = null;
            transaction.next = null;

            firstTransaction = transaction;

        } else {
            transaction.previous = lastTransaction;
            transaction.next = null;

            lastTransaction.next = transaction;

        }
        lastTransaction = transaction;
        numTransactions++;

    }

    public Transaction RemoveTransaction() throws EmptyQueueException {
        Transaction temp = null;
        if (numTransactions == 0) {
            throw new EmptyQueueException();
        } else if (numTransactions == 1) {
            temp = firstTransaction;

            firstTransaction = null;
            lastTransaction = null;
        } else{
            temp = firstTransaction;

            firstTransaction = firstTransaction.next;
            firstTransaction.previous = null;
            temp.next = null;
        }

        numTransactions--;
        return temp;

    }

    public int size() {
        return numTransactions;
    }
}
