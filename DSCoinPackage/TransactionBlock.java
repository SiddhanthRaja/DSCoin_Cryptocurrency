package DSCoinPackage;

import HelperClasses.MerkleTree;
import HelperClasses.CRF;

public class TransactionBlock {

    public Transaction[] trarray;
    public TransactionBlock previous;
    public MerkleTree Tree;
    public String trsummary;
    public String nonce;
    public String dgst;

    TransactionBlock(Transaction[] t) {
        this.trarray = t;
        this.previous = null;
        this.nonce = null;

        MerkleTree mTree = new MerkleTree();
        this.trsummary = mTree.Build(t);
        this.Tree = mTree;

        this.dgst = null;
    }

    public boolean checkTransaction(Transaction t) {
        if (t.coinsrc_block == null) {
            return true;
        }

        boolean isValid = false;
        for (int i = 0; i < t.coinsrc_block.trarray.length; i++) {
            if (t.coinsrc_block.trarray[i].Destination == t.Source && t.coinsrc_block.trarray[i].coinID.equals(t.coinID)) {
                isValid = true;
                break;
            }
        }

        if (isValid) {
            TransactionBlock block = this.previous;
            while (block != t.coinsrc_block) {
                for (int i = 0; i < block.trarray.length; i++) {
                    if (block.trarray[i].coinID.equals(t.coinID)) {
                        return false;
                    }
                }
                block = block.previous;
            }
        }

        return isValid;
    }
}
