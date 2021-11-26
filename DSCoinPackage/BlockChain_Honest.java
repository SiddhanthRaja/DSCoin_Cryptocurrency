package DSCoinPackage;

import HelperClasses.CRF;

public class BlockChain_Honest {

    public int tr_count;
    public static final String start_string = "DSCoin";
    public TransactionBlock lastBlock;

    public void InsertBlock_Honest(TransactionBlock newBlock) {
        CRF obj = new CRF(64);
        int initNonce = 1000000001;
        String prev_dgst = start_string;

        if (lastBlock != null) {
            prev_dgst = lastBlock.dgst;
        }

        String dgst = obj.Fn(prev_dgst + "#" + newBlock.trsummary + "#" + initNonce);

        while (!dgst.substring(0, 4).equals("0000")) {
            initNonce++;
            dgst = obj.Fn(prev_dgst + "#" + newBlock.trsummary + "#" + initNonce);

        }

        newBlock.nonce = "" + initNonce;
        newBlock.dgst = dgst;
        newBlock.previous = lastBlock;

        lastBlock = newBlock;
    }
}
