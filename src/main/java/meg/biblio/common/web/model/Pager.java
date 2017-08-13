package meg.biblio.common.web.model;

public class Pager {

    private int resultsperpage;
    private int currentpage;
    private int resultcount;

    public Pager() {
        super();
        resultsperpage = -1;
        currentpage = 0;
        resultcount = -1;
    }

    public int getResultsperpage() {
        return resultsperpage;
    }

    public void setResultsperpage(int resultsperpage) {
        this.resultsperpage = resultsperpage;
    }

    public int getCurrentpage() {
        return currentpage;
    }

    public void setCurrentpage(int currentpage) {
        this.currentpage = currentpage;
    }

    public int getResultcount() {
        return resultcount;
    }

    public void setResultcount(int resultcount) {
        this.resultcount = resultcount;
    }

    public int getMaxResults() {
        if (this.resultsperpage > 0) {
            // the maximum results are the results per page
            return resultsperpage;
        }
        return 500;
    }

    public int getFirstResult() {
        if (this.resultsperpage > 0) {
            // first result is page number * results per page
            int firstresult = currentpage * resultsperpage;
            return firstresult;
        }
        return 0;
    }


    public void gotoPage(String pageparam) {
        if (pageparam != null) {
            if (pageparam.toLowerCase().equals("first")) {
                this.currentpage = 0;
            } else if (pageparam.toLowerCase().equals("prev")) {
                this.currentpage = Math.max(0, this.currentpage - 1);
            } else if (pageparam.toLowerCase().equals("next")) {
                this.currentpage = this.currentpage + 1;
            } else if (pageparam.toLowerCase().equals("last")) {
                this.currentpage = getLastPage();
            }

        }

    }

    private int getLastPage() {
        int page = resultcount > 0 ? (int) Math.ceil(resultcount * 1d / resultsperpage * 1d) - 1 : 0;
        return page;
    }

    public boolean hasPrevious() {
        return currentpage > 0;
    }

    public boolean hasNext() {
        return currentpage < getLastPage();
    }
}
