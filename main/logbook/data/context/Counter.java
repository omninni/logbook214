/**
 * 
 */
package logbook.data.context;

/**
 */
public class Counter {

    private int[] counter = new int[11];

    /**
     * @return counter
     */
    public int[] getCounter() {
        return this.counter;
    }

    /**
     * @param counter セットする counter
     */
    public void setCounter(int[] counter) {
        this.counter = counter;
    }

    public void incrementKenzo() {
        this.counter[3]++;
    }

    public int getKenzo() {
        return this.counter[3];
    }

    public void incrementKaihatsu() {
        this.counter[4]++;
    }

    public int getKaihatsu() {
        return this.counter[4];
    }

    public void incrementKaitai() {
        this.counter[5]++;
    }

    public int getKaitai() {
        return this.counter[5];
    }

    public void incrementHaiki() {
        this.counter[6]++;
    }

    public int getHaiki() {
        return this.counter[6];
    }

    public void incrementKyoka() {
        this.counter[7]++;
    }

    public int getKyoka() {
        return this.counter[7];
    }

    public void incrementRemodelitem() {
        this.counter[8]++;
    }

    public int getRemodelitem() {
        return this.counter[8];
    }

    public void incrementNyukyo() {
        this.counter[9]++;
    }

    public int getNyukyo() {
        return this.counter[9];
    }

    public void incrementHokyu() {
        this.counter[10]++;
    }

    public int getHokyu() {
        return this.counter[10];
    }
}
