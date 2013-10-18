package de.matrixweb.smaller;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.pipeline.Result;
import de.matrixweb.smaller.resource.Type;

/**
 * @author marwol
 */
public abstract class AbstractToolTest extends AbstractBaseTest {

  /**
   * @throws Exception
   */
  @Test
  public void testCoffeeScript() throws Exception {
    runToolChain("coffeeScript", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String basicMin = result.get(Type.JS).getContents();
        assertOutput(
            basicMin,
            "(function() {\n  var square;\n\n  square = function(x) {\n    return x * x;\n  };\n\n}).call(this);\n");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testMixedCoffeeScript() throws Exception {
    runToolChain("mixedCoffeeScript", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String basicMin = result.get(Type.JS).getContents();
        assertOutput(
            basicMin,
            "(function(){window.square=function(a){return a*a}}).call(this);function blub(){alert(\"blub\")};");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testClosure() throws Exception {
    runToolChain("closure", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String basicMin = result.get(Type.JS).getContents();
        assertThat(
            basicMin,
            is("(function(){alert(\"Test1\")})()(function(){alert(\"Test 2\")})();"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUglifyJs() throws Exception {
    runToolChain("uglify", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        assertOutput(result.get(Type.JS).getContents(),
            "(function(){alert(\"Test1\")})()(function(){var e=\"Test 2\";alert(e)})()");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testClosureUglify() throws Exception {
    runToolChain("closure-uglify", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String basicMin = result.get(Type.JS).getContents();
        assertThat(
            basicMin,
            is("(function(){alert(\"Test1\")})()(function(){alert(\"Test 2\")})()"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLessJs() throws Exception {
    runToolChain("lessjs", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String css = result.get(Type.CSS).getContents();
        assertThat(
            css,
            is("#header {\n  color: #4d926f;\n}\nh2 {\n  color: #4d926f;\n}\n.background {\n  background: url('some/where.png');\n}\n"));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLessJsIncludes() throws Exception {
    runToolChain("lessjs-includes", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        assertOutput(
            result.get(Type.CSS).getContents(),
            "#header {\n  color: #4d926f;\n}\nh2 {\n  color: #4d926f;\n}\n.background {\n  background: url('../some/where.png');\n}\n");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLessRelativeResolving() throws Exception {
    runToolChain("lessjs-relative-resolving", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        assertOutput(result.get(Type.CSS).getContents(),
            ".background {\n  background: url('../../some/where.png');\n}\n");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testLessJsVars() throws Exception {
    runToolChain("lessjs-vars", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        assertOutput(
            result.get(Type.CSS).getContents(),
            ".background {\n  background: url(\"/public/images/back.png\") no-repeat 0 0;\n}\n");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore("Currently sass does not work as expected")
  public void testSass() throws Exception {
    runToolChain("sass.zip", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String css = result.get(Type.CSS).getContents();
        assertThat(css, is(""));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testAny() throws Exception {
    runToolChain("any", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String basicMin = result.get(Type.JS).getContents();
        assertOutput(basicMin,
            "(function(){alert(\"Test1\")})()(function(){alert(\"Test 2\")})()");
        final String css = result.get(Type.CSS).getContents();
        assertOutput(
            css,
            "#header{color:#4d926f}h2{color:#4d926f;background-image:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIcAAACbCAYAAABI84jqAAAABmJLR0QA9gD2APbboEbJAABMi0lEQVR4XuzSQQ0AIRAEsOFy/lVusLEY4AXh12ro6O7swJd3kAM5kAPkQA7kQA7kQA7kQA7kQA74c6lq5tRi7zvgq6qyftctuamFNBIgdAIECL2KgCJSLChib1jxU8cpFuxjG2uZGRV0bDhjF7tgRQRRsYD0EmpoCYEkhBTSbnvr/2fvdy6BkUHf7yXfe98Jh3PLuefss/faa/1X3W63S6qqqkUkLPEJCeL3+/VlWN+JRPuiZcOGDb1btsxY2apVaynYurX1hk0bS/bv3+/3eDwiLvm3W8AfkES9Xqf27cXr9cq+ffukaFeRdOjQUdq2bSuhUEhvE+Z3e0pKZM+ePeJv8KM9/Ky0rFRiY+OkRYsWUl9XJy6X6DUqpb6hXlJSUiUQ8EswEOD76OhYvV5Yf98gdfW1EhsTK4FgkPcI6rG+vl5at24jycktZMfObSmVFVXdo6N943y+6JzExKTf68XLwuGQuF3oiypx67P5fD79LdrIdgo2HHG9Th07aruSpUHvd6Rt8KABv5E4mtnm0k5yu90DS0tLn9KOHVpbW/dpbW3Nxurq6mtqamoWtczIGKvEUY+OO8xmOtMlCfHxzvv/S1soDIII6MD5eV8Ji1sHNFRXV4vn6llf3/BtZmZmi2QlOr+/QfT9ieFQeLK45JvmyzmafsPMIeeI8nrTAg3+xXvL90pWq1ainGPC/urqCRs3bQTljBw0YICeElUfCAQORxpKWB4BZ9mxY7vo75QDxB6JGDHLOcM9HrwOclZGR8ccibDwPX+LtjToYEtI8nzRMeMSEpOHR0V5u3k83qSqqso1SgjjROTq2Ji4FlXVVQLeGBcXj0mQEQyHFuplrhOXTP8f4jj8xk5O"
                + "TUnN3rx50ydgq927dxdfVBRZacG2bbKzqEgmT5p0eatWWdVyhK20pFSqKyvFo7+PGEgSTXR0tGXJQvZNInOdk5mZNV4HNEMHLBgW1xYRuU9FzF6Ii0ZEYsUF2gziOiYmNma0iqAJMTExx6i4IIGGw0GKm4SEhDa7dxfPqKgovyUmNj7eH/BfXFZW5ob48Xo8OAfXf0pvMVSPT+i+WUT2/g9xcOPsk1atW18578sv//rj998nXH3ttbJ/f41U6QBXqhxesWqVtG/XrrRXbu5MnAsOc7hZrIMj5eXlsnr1anAgciOzEUvU1tRIYWGRpKamiO8AkbhcbvcLnTt1uiw2Lg6DDoKkrK+o2PdhwO9fAEKyBKUiThITA22ioqJGKEeaEB8XPyo5Obl9AzgeCFnb1lDfgPbhqSR8gHiAU65RwsxWLLJNzwu1yspyJyUlSZ2em+zzyu7de4CzLtD2XxAIBir1Ge8Rkb8enlv9/0EcBG4JiYkXaeffmL9uXe+vvvxSrpw6Var375evFy5Udh2UmtoasF8ZO+aEyS63SwFiPTu80UbCqK2tlbVr1pIovBxkpycxo6uqq7GTeAq2blFckpDZf8CAyxQHAFASBDaEhWB027Zt/RXj/JSYmOBTkJibmpo6Mis6eowSw3FKQF4QFwCkzxdFEFmjxCwuAWGBIxFTBJR7YEO7U1JTJ4JoQBQ7tu+Q+V99KUF/UFoooZ44foJyIr8+W60Sd1lSIBB6XJ+5pT7/LSBI/D5sQLTLRVH4/zZxUIykpkzNX5f/7P6a/fLdt9/KyaecIq2z28pL//qn7KuskC6dOn8anxD/1XEjR6zOysxcaIngcJvfH5Dly5ZzhsfFxTUWBXgPIiMXqNRrF+8qlvz8tcVLly7defkVV2Tn5uZKnRJXIECClZycro8rYd6mHMjnjfIl4pperwdcAdoQjhRTlaptJSclgiBIHN4or9TVKYeJSVBtpgG4h2ImqNeN8kbJvvJyJeDVMm/uXOnRs5dqZOv1s33i8Xp198jE0ybJtq0FUlNXe3PL9"
                + "PTF7fu2fbfBaFJ1nBhuIxabP3Gww45uc1kRcPH27duffeftWcpWd8vYceNkxMgR8tPiJZKRniEjhh8LlTJ1y9aCx3bs2CG7oW6iUyJmjSJ9nZEpsnTJElm2bJlMOn0SZDzEw78jSHKXtLR0Ofvsc2TXrqIuChBLMzLSs6l66h4d45P4+AQMpL4PpuF2IATs9XUBCRlV20V124drgtOR6ECPVZVVVHFjVMVNTErAeVTXPR43wXHBls2ycuUKuera38nM556TKZdeJp/M+UiUSAHAySFGjzlR6nftAnC9ze1yvwuigPh16Z8vxmsIv/kTh5X/R0L1mFlA9WDFsGGM8yQl/gsd2r5DB2nTpo2cOnGilJaWSVZmlvTonssOXvzzkiGK+KfvLd/3u7pG4gTXTEhMkLK9e+Xuu++WEceOUM2mJcRB43uTIFQVBuF4tB25MTGxJ6i4mJCbO3ocbBkVFZUUX4oh1G6zQzlLpeT17kNR5HKFqZruLi4G54HIIogNud0gDBA5+wDtowYVdoGT6Hu/VKhtJDk5EeAVIosaTW6PnnLvXX+WzKwsufm22+WOW26WabfdJqrliIJVJagknh8fHydlZXv7l5SUnt2pY4dZwCd79Vm7d8uxE6z5E8eWLVv+A2OXm7MrXllzeno6OunW8AEQKps3beKgxsfHYwAhEihjN23eJMXgKGPGbOjbO49gMHLAQVgguKeefFISzG/xvlevXmqwquA9raEtKyurQ0Vl5YsKSrtBe8C9IBLAYdDhGFy0LTOzJdXM3592GkVcz7ze8sZrr8qEk05SzjZen0FFSIsUHfR9IDheOxSiFoQdoJRsHyQcF+fRNtUAUINjAOvwPunKFadcdpn85S/3yaQzzpSLplwizz49Q8674CJ5V7lo334DaCsJBUO8nrb5LX3WfAW9KwGyyTn"
                + "/u3COMrUmHmnTmSpr1qyhdXL48OH91CI5WB8a1kcMkJGhYXYsPgd3yV+/QTWDxFC3nC6vuj1uAj9sxrLJff78+bJ02VLpoFbDZcuXybPPPSt33XUXziEgFHHx+olJieGOnTqO3qccImy0Hb2nAXgugsStW7fKou++kxYpKdJvwABaVRMT4jF7ZdZbs+RSHdCVK1fJnTrTz1SRlNuzJ89xu6GVCEQSBpOEWaGYJk45Sk1tHcVcbCw5DttTUrJHzjv/Qvnph+9l8mmnyoo1+ZwgL/9zppw++SwpV2JNSk7CBMH1yI2Wr1z1jeKcTBV/dRBl4FJouza+WRMHB/TIG4EkuMJIZcUf6R6Ljk3Qzv/TDdeDfWMQMNthiIKpXG0b2+X4kSNeUALZqx2E78z9aOIGxyLBpehgqkmdomnmSzOlW7ducuqpp6iIKsFvaLWsravZplxlnYqDXFzLEJntfALGtLRUufDc82TEqJHy5Izp/CzK61EssEx+/PFHjsMmNcStW7dW2qpZnr+jFqH3cOu13B6Kpv011aIiiyqtmsn1GeP42mNsGiBMtPfe+x/S+50tY44bSdyR3jJDRh43Sqqr9uv31cAX4GLEKslKvfosr6qa7iovryhLSWkxFUSPZ2jWxJGXl/dLYEPCRqy0yc6+Xgf6cdghrEbh8xH8QXZzNsTGRgu+X7N2nbLftMCQwYOmRVoyLdcoUX/I4sWLMePZ6Xag1S4ts2bNkpNOOllaK7HAVsLfUQ2UNdqOXNxLjyQGQecr8NyrhNleB/yGaTfJs888AxFFjNAyI121miLiIGyzP/xQr5st7dq1U3V0O68t+GeAeXVJtaSlptGPUlOzn8NbW0e8oZwA4iYkXvGoKCuT9IwM+ccLM+Weu+6U4uJdcvU118rOwp2qMT"
                + "UYIxrOFamtD0CLAlFNBidTiyve/zMuPm4ROa64mi9xpCmG+KXNcwC4ZaWlpT2ug2hYsZsDiw0iBLMwFMIAe+gI27W7WFR9/Yuy0ApgAjPTIbt5/jfffIOBIXHZ2UNDmqL9lStXyvOqBdykAw1gZ6+fkJS4HdwLr5U9y/r8fCWwJdK1a470HzgQoFQxwBls29YtW6WbWmj37CmRjRs2yO/+8EeZN2++nK7fz/38C3n91VfknPPOU4NaIf3arhDtGwCR4CZGbPmNqHRh9uP5KFpC5HxRUqLaV0JCojw5/RngMVyLv3N7MAlcbGdNbS0dkPGqkUX5fLzWNuWo9Q3+99WtkAmCBOE1V+KgTv9vNoPuVSoHg7OAG4ADMIggDIAuDDzEEs6LiYmmjWFLwTaIiprBAwc+iNmWqmIDm5Wv36o9pKKiQmC4wrGRZkLx8sabb1CcnDbxNIBMakFlpaWLFPNcr0YsWbZ0GTSSpd1zu5++bu06j3qEZ6s46lVevk+xxaUgCoqDmS++IO0Vzxw/+nhZpXjjhBOOB2ejTeasc85VwvdA3dVnJHFKog72vgoQv4eDWt9QxwHl7gYFm9cuWmyVgOqlpLQO6i98OuyDYIjeV/QbgDrVdU4iJaBqJfQwJ0KgpRLh00rkcEaSe7iaIXEcXu4RLFEMdN5XUT5XuUdHUD4eGrMXM8kYkfg+KsoH7YM4Qu0ayjVGPq3vG7YUFJjOdO"
                + "s56l7fvZsiBQOMwThMWyiusD3//POy8OuFMmHCBOnSpQsA7Weqjm5TAkxUwnhducx1cO5ltMzI27x5856MjAw422DKJkfauHETRdWNN02D2R1iSkHrdjnu+OMFBjO0BW3DIIaUdbjCHHiow9RMoCJ7Q1F07/MZ0D4DSvH8Xi/ESzm+B2BGyAAnWpQv2k4GaHCcAHvUVxQbE4M+MtglpKr1nqu1nc/o866CeGmGxEGx8W+5hlL1GzrwHePiYvFAeDh0FGdHNC2dYLcBsFA+tKqb6LiQDuSMd9/7QNauyxe1kGLUeU5aaqrigBQlMocgje1jsBJeDz2nUAlkLgiuo8748opymaXqYfv27TSWo8P+jh06ddDvRoUklLzo+0UvaZsmKSdIBhEMGzZMcIQowobjRFVpU9PSqLraNoADwjYDVRZWT3AoaBYuPksQzwi3ADgTtA4+H+jYFQ6jr+C70e+qVMXV7xOTlIsmCNiKvx7EUaz3rYP5nsc9SqgqPnBfi5/Qr/geWo+2q+K1rjldegPg6nM3P+JwH0ocGGh07kna+YMAOEXCluIx+OzoyopKslFqOzTqwJyeKt1yuta7RB5MSkxIVvP6fTorvg8bIgD6x/2MxgEucaYebtZzBioewXvMvjOVK72r54PNY/bBtJ67Y2fh6PJ9FSMqKyqG6qxuD1CphEGCOOeccwAi8R73IWFkZ2fzO+U2EBVoshURAKy0Q6g3Fp5VEoN+DC5C7ojJQK2kej80MthocC3gH3AC/q5ddrYeY8k9Kiqr6ChEX4GocA3gEvXegrOCiEBQvAYHLcpD0bxp8+Y8Fa9n9uzR/R3YU/5PC5ffXLxFLXiHIxh04Ba1bnakL0TC1qRNDPDcs8/J5598Ki/88yUjexsMUblBLGChHJC5875Cp3RSA1UBzNawPOopwCbonPs1Uuy2BQsWgCjwHgMH0"
                + "fS2Ypkb9Lr9dR+jnx+rtoa+1mM7ePBgufjii0kM2EBwuKfaXvB7236+LikthS0GA0xA6HZ5ADjxLATW/kCAGlIDsVQYWhE4CV5DC4JX2RjKQhhoiCttZyIHe7+KKqrvnCwEoHx+en/VAYcNBAMRCiswcAy4GMQZiBMbCE2frbBfn97ZGjh0iKgdOXJ4c3K8sWMx8yZqB3bUAUCHQHUDyASaJ6vcvHGj9OnXFzOM7DkSqniI1mkQI+LPyel8Wp+8vL9rKIT8sGgRwCK1jYKCgqs///xzAlNj8cTMxPXG6z3PVIJx6efoPDvjMFBqA5kIi6m1ohqzN/EPiViE3AExIRgsiCMcwdbhOcW5/F2yigzMVt0M9tBjlNdek8YsjWDjAMfqc2cquAQRB8OKF/QZQkGKIBJTMBACkYBA6LhLjEqiGuyjk7ASfUEwukiB8EAlblwHogRYpay0rM3a/PxzO3bs8CaIqll7ZV0uqnIP4wHi4mPl9Vdel7ETxpKqF323SGfvXvo/Hn7sUXANcAF0PkCp0dlD+ByDTHCY271bHMDi+vXr5euvv6Y9QiQd5+Qpwa3"
                + "SQU1Bp1pDmYqLRBPUg3NAaNgNGI6FlmRVXxtLSqLRgwGLHmhRZP2tW7dSlbMUmMKqpFQz41QcVOhgKWfUAaQ3lgRRva9Sj3Sxc1IkJ7UkPihXLgPOA5UV6inAdVDbyngPuvR1D7rg+kf7dGcsCBx2vG+Wuhneev01mT79SZky5VK55PIrYFWFaMN9MBkfVBfCm3ExxHbNyULqakwYI/TYvaWKj5kvvCgvv/SSeiI3KQglpqA8P33SJAwyRJLxH3gAwMg6QVRgucuWrxAQxaBBg975+OOP5SW9TufOnfVczs60fv36TVAx4f7uu++gvho57bUufRvgGxmgCwLi/R3N2BWpXFlCAmHR8aesGrMTbQNugSGLszhsfl9dVQ1igLoJMEqXe0pqC9pjfFHRIBiAbLQFBMfA4NhQjARAoEZzwcFlj2HiFtpOMMRRviiIKPbrrqIiGhy3qfV4zapVqmVBld9HrllcvKfD2rX547KyMj+vranFtZsd58DgQA7ehodB8MumDRvV79EJcpTsvXOXLmSnkyafAZmN8yHzadhZsy4fXlZ/Rnq6YsbKtKJduwMt01Pve+zRhzd89NHsFj179myj6udQETlBOc+pykUSYDgCARkfCY7/zktsgSYtjA5B2O/se8aQsq3hqrC69HdhoPU9xZZys3oQAAgCBEQrqzrEoIEYfw9VV8Z4V"
                + "FRUW+0CBAtCMbEfyeKvrhLek/8ZqnCxIeZoAHhUNNvcoHunLp3ZljZt26nNZYVMnHQG4lKMO4Gm+9uVUD5HXAgeprkRBy2hehivR4KmW++4nbIZ2zMznpaP1Px8rloW0XSwTjwsDFQ/L10uhUVF5du3FbymlsNuyoLHaGeHtmzeNEwHaL3OmGwd+DgFoBh8YAsSglpdbSiAHGmzxAOCMu/NZxZngGuI5X5w+oEAwH1AJAj1AwHoe7+E3QCQQYhC7Dy/lk7E/byWm2IlCmzfRqTr9eJVrNTALQ+Vluo828E/52htJ3WBOjwnLNDEbCedfKrcf+/d8tOPP8hxo08AULYaIIKVAdxHAOelp6UV4Ltmk9TkzLrQRY6892DQiMhhxeuhXswTx46V0SeMRuQTZhsfLF+5yz5F3Vs2bYhZvXL5hVVVFSfW7K9yNdTX+pRTjFcvblcFnXEYJFwHGyyHAJVWrf0PN7YLgUM2uNghEnaFxT4gWn5uzP14zd8yHUJo9cUA4z0tpNbeExMdAzxh8Qv+gUiQy8IBdBn7CYAmmUQEl7BHI+0ogperx3mdOhc1+Jnc97Irr0KIIYEpxSiBNu9BblZUtOsKAfc0GKwZWUgZ23BBkCjch9dKIHx6HdRaGXXcKOPiL6dW4I2OJlFofgpBakN9XWyvvN6xwCA2UryS8pocxvpRsP9a0AX5jA6EWgmug3aZazI1QbFOA2YkOhfiz6rXVL/x1sRwUhRYe04sLJougaNQsdVWGMyoftY3BNAPwFU0eNHUHgzRqOVwKyHAxeZclq947KkY43l1BGa2aqUYrYMgCPqBRx6R115+Gc/A+9SRuOmphsp7rmKl26363PSA1GHPHdS72AfyGS54NA6DC3QeEx"
                + "1tbQdmxriB6Gl32K8DtL+amV6WE1gCwHuwbHz2mx4WBIvrYHbl5+cjpBDEquKi2IoTbDZ4l3YWECnAMVRRuOL9/iCIARZNaClQbanRaGA0bQ8L1RkYVNB6zvnnkcuIy6Nq924OYmpaKkArsIfVxCLxjtMvYXsU2kjyNNhouEa43XLj9Sqi/8w4E82Sg+Zj0yKwW4Mg2tmppKRsYIvkpCXqnGt64nBM5bWTcEQj0WDo4RgQBZiQuRxgh5W7QQDQShhxTU3FdQi1WzzB3brmj7DZnBKIILBitAX4gSAzO7uNKLDFdTm40dFRBJGRqq+bXmRco8FqNoheAxEDZAN/IDgZmEqNec9Kjx495IILL5QX1BOc06279O7dm0HDbpcXbYDpnqmZtXXUeAAucQ/YNiyH4D9nJ7XQ7lFUVChnK0ZDjMhNf/q95CIgOX+ddMnppip7exV55eIxFlr0N/xOGampQ1QrWgInZ3MQK1ZtHKuBu2YQGS8JNs6BiPKqWbu+Hp8bwEacQtETxG8DQaiJjTPQcG3LcX6JMMERwIVARCAEyGsQAXGJ4haoujDCYSdHKNhSQDtC69atjRhB+/0cPBGKLs7OVL0WZrnm09C0vXbtWvnD734n99x3n/zx+j/JU088IV27ddO9q0w8/XRoJFBvCS0pXmN8ZqLUApvwnjR2+fFdNO/DwJ4wQ3sc+uAf+4GxHpdcdoUa4zrJ0zOeVG4VJ2efe54Fo3TYVanWkhAfpziulfiDwdEazjgD/d8cxAoGJVbZ8yi8hlwF28XDAZmTWEQcvwoJioOKmYo3kYYqa4uI5BgYULBkAwrDkaozItfJrUaMGCFdVN3r1KkzrKYkhEYbLa5Kj1ABqXImJSVTFdXNhA7g2gSfdGz9c+ZMef7ZZxit/tHHn5AIFsyfr5rXDDlb/TFn6d5WfTQijDpnTGxdfR2uQWDqdhGUEzOF6kNUXcPBMMWRDqASiR8xHcYcbhCqlSs4uNm3DHoeoo7Bbrk9"
                + "QEyYAIx0s1bfNnpfTC6EHGgw9hk6KZK1vyqaQ2oCxEYfZc+xJmjXGpcAwszAM2ueD+t2W92fbBziCEBKcUclBgvcBmCRYsD4ZRiIU1xcDO5gBx3XpWGpT58+cuGFF0FkNCYEtMvgCbbB2kOQl0Lg6HIf1t5hRGC0jBs3jmbwxx5+WJYbnPKyBvqcqTaGDu3aysuvva7+ixFwnFnAi8h0XIcD7PV4ibkIUOsaRAxHwqyG1gH8EBcfr3ucaPY9+4Y/dXZoRlCHgV+gSSEUAOIKfQiHHiYC71FQsBVcGgQH8TdCraVzmkGwTwNY7zBYFONi48l+6ZTyN8BAxQHSuEcMjzAoxnAJj5EtA/r3Y6kAX5RHxUEaHh5EwtlmOQ0dV2oPefudt2XJkiUgGgwyHU+TJ08GYZjcVccYhs0Shj1iQ7vACfaWlcEzbGM7DTClpgDxwntkqsi4+957pFS5yJjjj5PV+eulm3KPd95/T0YcM1wee+ghOU8xQVJiIjQcDjIjsw5wU1pLdxYWIuBYz0k2vhOPbF6xie/bKoEhNDAlJRnqKL9vBEBIZPhDHg0m3vbt2wBuEQgNP4zxTYWNh9uF/oV9JE8NinOa2s5hAle8nTUIVo+IySgHa+UAW5UtLi4G8hYdCHGDGYU4DXooBw3sL6O1449VVA5wBxxg4j84+/1+OroQlKMhfd3QgZYD0N7x1FNPITTQpAVYIuAxkiNY1RHXxX1hqgcnoghptNnzEFnPzp+u6uToE"
                + "8bICGXtNkXib4o3lq1aKZdePEUCJlQP7B4DyNuZWZ+uBF9auhdAFp5fxS3rEFTEvBWUZoA2VFdbj8lgCEsid5vzQ/FXpqIkBYFOwQAy5/AbOP/YP+RawaC1BA8Jh5telSXFakPy0NnQ62mPSAhBC8DAGuKIU9aawtmJDe2GTIZnFCIDD2nObZQL6tLvYsgVPlDr6hfqhTXqIDqNwPPTTz/FaxAJOomvrRbgmNXFUVl5D6ZFQmNhO0WIEyyXsRsICJZHOuxmf/KJ9FROMkENeQu+Wcg2vfH6G3LbLbfI+BPHQGMBpwQ45rUs5opVQm+VmQknIn0qySpKUlLS4LsB7sGgQkyQw3q8BJlsiaFztJN9VFmxT4F1axVLyjXdHmoyxhKqvyeRgbj0SA7UDRHvTc050MHROsP7aoPY6WtWr6EnU8PnTcieE46v56IjyFka/AFgEqurH0oYznuy5id1psK6aUIEqaZu2rSJ2shpGrHliJHIa4ixVzAQiPLdF00OY0LzGiw8Opy/xRq6MOPJ9r9WJx/iR6+47HIAYXUFnCtvv/uufLVgvvz4w4/aliwQZKR/hO2Ev6NtW0ajIT/Y5MyE0F+4vk3ThH3FclvsmAgEmdUqlrQSAGwnvC/zat1M3sZktA3GtRCXCrDbVq8d1xzM521EwsirgIOKNouHHnhA3nn7bcwu5nAw8trtGMwiZ6ad5REfO+8josqHDz8Werwi952wqoKFMnDnH//4h4wZMwYEaH4TyXViEUqnBLuaOS75a2GwKtFZVyRbCwqQqkhx5xBGOFIMmdckEMx8uvAXqLHrRc2P+Uw5CbYBAweojeMFyenahSUVsEWopEY1D5NT1FFtDkYSpPUmR7ogwAFB1BRFfu3DLjm8NrELg5MMIWCjm9/J67HiOF7vldPUxIGGphsRT1Px2HFj5VhVK++562654/bbTT5ptDPgPDoD4fRU5IAc5Ell/MQjjzwsTz/9jFx11V"
                + "Uybdo0uU9tDTfffDNiRQ0OidQ4KO7AaVBugaLDakslivqRc9JOQwaydTaDm1mMcvgaGC6rMvL5hh87XO6/7y/SoVNHzvKiomI594LzZcoll0oRnXqO6DIc0blSoz0yuYqDHGIANvusUCdBQnyserI7M60SlQhs1h/7NDaW0XKlJSUIooK5PzJNFH3SrhkYwSQNKB+xB9AEHnvkUXgSVc17TZ7TWY1ZiuhveF+NBZIs1ZkljZvgco6OkYudNmzY0MgTrW0kcrabzmZKpWzSCPL+A/pTFJnNnG9jQRsH5hJ3WAKLJBrLAfgcN91yMwNxiotL8CxUi7GZqj4QG0hqIlfyxcQaTSpgb+HYMfiabeHHviifEm8hYk/JpZK03YgasxFiIQJfakL0s6S0oOqPFoKzoI/gsrAJ7ilNThw//vh9FjQNdDI6p7faHd6e9ZZ8q+wXxHLLtJvlz3ffpRbLHiojaxENRflPs2+AVtRGA2xkrrNZ2c0Z0fhzC04dLyRZLllyamoLEAYBmxPMY49hByCHfyk/2QJV3g9thcHLWHjpb0G7kI4AdRtpDDhbTAUiGgQzMlqSyFnMBfd3rq0EwSGgzUYDWZSggjTm"
                + "gQx2MbOf3mJej22JcD6m63XBFWFFnT9vLisDxCUk2PyW5KYWK8jhaGEsmwSbuaqOnnHGZPmvq69Rg9GrMuyYYwCcqJbuLCqkNRBF0zRXBKwUct+IBdehosVJnMYAkAu99OJM+dvjj8utKlJuuWkako0waGDNnNmNE7idmiDC3dFmDksMh21D5Gu3G4QaALGCnaPtzIorVmIEFwHQhdaSrUnjnTt3oVGquLgIEydSwCA/BTMdsRrgcBhQ1hrpqBbemro6OOzwTIe6DpxJxD7dtm0r2sD0Sq09hmsClILY4pucOLKyWicw5jFMczbkLqO2l2kswjcLv5EbbrxBhqo4QLQXEHaHDu1hD2BsZUulfPzWIYRD40RsdcDXXnlVLle5jlNHjBzJ5KJ16uvo06e3PD19utUsnIEPheGQslfCbn01IDS01RLf0YpRm5oJgMxErPjERFQQIP7JUNsLraAsN8lyT1RZLaejKg6AW1xMoqirqYVtB6Z3RKxDq4PmAjF9KMHappo2gBBRJehtzfCLi41jLVerusfDuNSkxEF2WBplXAJ8cKv6XXv11fKIWhDhjMKOQYHpGyzerX8a74jPbMDv4WazjahSzecdufKKK+VBNWNfcvllMnDQIBk3frx8+PEcufD8C+Ta666DdhRZZQiBv8QqjnMwDJsDZjdEDmYsQCuIFCz6aIgD3lz8HiZ95tR2YNY90jv3KZfcb4K"
                + "XYSqH+Ry5K0nAQWgPzObggPRCZ6nq2zmnKwYZxjUGQYVMLItDCNwpWsSx/pITbVyfT4vy8BEjZfWqlbY2CEAp1HZPMyCO8rABmDQjr9cGwyDz8iuvyMWXTJElixdT7obFyRMNhYPoKMpsS+kikeyeu42sllsVAF485WItbNLXynLu2B569BFJTkjQ+hb/snYNfAdPLGYl1F6wf6rZZTrTly9fzkEo2VOCtiHQCKGMBuxJY0Jt3Ca2n7ijqgoeWTwrRUA4TKyD57GxG3Te7S7ezVm/fds2ZMfDZ0RjltYLwUwnkVVXV8HYh/7ADq5kCdaKFQQMwaJq8RL9LDbXJiurFfxFdEn4IaJNY5uaOGDCrsMsMcYfIG2qiueperdyxUpY/SJZt9FUhEDq8OUDQgeFHq7SSOtNyrqhdWBD7GbERpY8+vjRSHcAKLSgkYSV3a4tHWble8tR/wuBPsQBffr2kV55eSgLIStWrICsRuf+EgexfhfuPI+R4tKYuPnnmO9dOrNTwNEgXkCwcJSBEJl/U1dbQ/HmctN7S8IOmrgXPce4/kNIcmL1ouw2rYDRIC5BKMq1crWPl6thbqGq5h3onPO66cWG38nfxMTBQa42g06bwaXK9jfrYKJjYM1DDqrfD1UuiLgNlHcE+4NzjsfGLDuSl9o65i5TqsFukVoHtuSUFHCiyLxdsGmmP3TJyUHBFSXYnajeh5A/mMShaiJghl7d/Pz1ANNUF53NajCNBj9M0z8i5ZFLwxltrbaOKuKCaRv2GajxTMJiJcF9lbB44lyDeYyY4LMQQAOwQ+OBEw+fkdAzW2ZovyVqHzBfFpF1EM+0+J4y8XTm7haqW5+aG9vMONKGZmDnCJWFKRPBXktxpCdzgNa8uOCCCzBomBWgfNbeUGmM2QG/CryfGCwbsdXIJyLcemiBtY46K374/gcjNrwAe+QwluUuW7qUnIXmeod4AOwQewkLq7XOgsgMUTKugzO5eNduAsu83nkMzzuUAO3ODe2FXwcTgxgiKwv5LUkI9LHcj3iJM1zvx+IxmIletwXLEjZg+VDA7IUPhs7J8n3lajbvyL7YUrCdRBUXy7gWzBZoQngGAHOkgJgQRObR4Jnzm5pzAFCW6SBDqwArA7dgtPQ"
                + "dd96hZQta62csnsKEHlb/K92rwDAe4XMAawCrVtXEFnkkYSUpGyXgfO9dFRErOMCRM/yF55+ndHpFjW7YEO0OAtXTDIHUgDWbIKIGgmDnPozSUvd3CyGOMXGXjsiOtORKpLqNQaDrv7USHgAuCIWDHj6YlNyUPTyiX8A5gTHQjsOGODLy3dhq2rfLBubS6+8GMEfEviVcBgnNm/uFvPvWW8yhbZ3dBmGDIET0M9TZcBMTBwHgBu3AGgyy1+uDcwlaAFRX6Nqc6Tb4Jj0tVTSnU+UiWSo6FL4YG4MRCQB5tIHF12vdsN9r3c6LFMd8PHuOuAxYXTB/gXyvKZaz53xsNSQ1l6+yZmYnyIfJzRYvHMQVTEQYwJwLtgZ2ieOAg4oeKWYcMAgtZ/u2rfCOUo0lYbnJ/Sx1ATdAfQeXIWdZvXKlAueXAJLJ5Q4Fv/y9BEjM8JEEiJdCxrsc4nNwJ9F3V656oar3MLIhKAnlKo3rH6KzvqnFCoJ7ClWtUgJvwNNCdgOp0/JJnBEMctY4NUsDwtnvjkY/wK1tMtel0UCIKdiyh7PsielPySJNpL7vnnvgg0DtDGSvQe6rl/QyWmRr1Pgzdux44384SMtopH1wjzSy4YaoOIwOZrstnnGcb15TnjIEQsQsJYF2zukCtRKDCN8N2kbTNk3ZFZU0+P28YAmJ+pM5s+X6G29Uu88xsnPHDkOkLD/htNGsIAGRiGt5rT8lGFYswkh+0BA4CmwjEGXs1zhfHH5nQgv90MDWNzlxVFTQU9gAsQGRokRibAdMCDZZ81RhI2akU8ObIokPZKOxwMxsVJabYO9mre+14UDpSVnw1XzppcASxIHgIIQUQqW8eMoUVADGZxRHzuAecrQxrNY5ZjUn2BLI9fx+agkgbAyQiXar0SOBNYQgsBICdmBKR1QZOAHZe6z+frWGLXymhDxg0CAQHp18PXv1lDdnvakD2oafhRwD3UFEizfkFPDMMrt/P1RgtsVjcoFL91J06ud1CDuA4c0Y2GIBhBG0Xaoa0rYmJg6KlQZ1Qi3"
                + "XBx6XmZmhHUtucVDMJjbHZ+LMFkRi075gw/lIRCFzpChCGiFF1afqIj9Ny1c/8dST0q9/fxAhVdEPdTZGbiYzzt7XEAIGPkqP+J41SoH2IdL0Pdk0wB1AMrUoW4TN+PKsd5XGr4SEVFTsoRgpUfe/mJhUJDp/+MEHVJeLCotYkdnih+HHHktOge1nDXMcN2ECBz8csv0UafmM9CdBA3NRLJGLeL2M/LKLAzAA28vieyBcEzhUi4vs0GcINX3ZpwPJSJs0WmncgfC7oO1c+x0JoTH3IOFQqgPJ288tgTizCd7Pq6+9juzY2Zx64iASa57mb5xit7aQPc7FzLcYxtybLBhtxLnoYHBA4gl+bjgL2s+cWMz2MINyaETDxsgtM5pQQffrjsj0oepPOvPss4lHEJG1bs1aJj8dM2SIfK3FZi5ULof7mZxfy9XYE3xvnXpuNw2L1fRNNcCLjOcx1YQCBkOFDZfD+yi0ERN2JZ69OSQ1oXN/0hl7LajXZLRxh6ptiuBzRjucxA6+8GG4OUE/ZlDwmjsAJsSPU7nQCaLR3W+vxwGMivaBDSMoGBqFMUcz4srGOpgCdb7IIBuog8hMg5scA2o5CtrN+9aGHYulrRNKQtMdhj8sHoRI9A2a+/uBBiCvUONUdnZbaDQofUlV82ItRnvdNVeDaCCS6F538BCJ1go/iB22G30KrGGYrnE5OJgIBA+iJCci0XNSLsE5zSXLfpFNz3Nc4VxSCywesw0mbATWYtCtIQkyFOwSg2bAa8ionfUgBjPLsTPdj6ZnZ/ZHek6pvRD/lG0txSwCjgD2AavFjvuhU23KgikRDVc+MQ7SBXjt2po6Hi1YdfAROj8St3DHK4hHaAesi963Xz95YeYLsnb1aoYvbNOUga1qQ5kw4SRZl78OLabTjVbQ0MHXEksowkBjm7fD1zzHOddqgPjenEvPuK2dthhqdRMTh01W8mzShm3XRrfTo2KBNkDOiJxCdjlAFSyDJmeFouagROkdO7ZZe4e1MtpCKbgOOgthdrAPMH3B4+HssfYRXAseUgTdMGEoJSUTOSHoRKB6YgswHcf7a1+7IzlRIyttJE"
                + "cThzA4OI0G1AwcrK65PXJVc4gmJ0pQR9/8+fNYx6O7AmXUVweRA1yD69nHde7lqFdOm9jHtg1WjMECaiYaUykwARgCEWpoKFcN8keIpOaQDmkdTgv09cUIoH37rVksGT31v64iQCI6ZwnrWuM0a9Adct1iA58OJss24L3djaEIBEWtBk4q7MhdbRzaBycXzOUAb7AaMjOscbCyHWDnyJ3YgTRJ7sfRPsQoZ939eBty2HokkUC8UFx07NyJwHPHziIliG0yatRxCGWA1ZL1TNfnrwdng0GMKnFkW6xocZEbMHsQ76ythvexIo+aS0kJ8o2hqQFMw67yrds44JpDgLFJPg59BsCEbYmWjV6o9bumP/UUWDjc56bISAJUT3gR6eaOio5G0AuCYyAyIHZswjRYpp31wA7W02oK0O21EWCROSzwkRjDW7TtbGv0MgvtHRoNJoJwft4T2CIyYTvy+mgfnsVWKTIVARytTP8Q54n4Fhk4YCBtIU/PmM4kr4GDBiMYCPXKaOOY/9U8lMgG9oAIcESVOP4ZAOZqk4yuHlv0h8mnCXJyxWg/UCtzexBYBJwFmwdWevgUfYOJ1xyIwxagnaPUHYQ6eOLYE+UN1enbaaNf0VoSXgMIYfuwg15bX0egRY2DVXECxmVtBw3Hg7gTtQ4bIeUMNkUHgB8caUwLBJF6PJZ47HUOfW2PLpPVjkx6DFZEPXUQIwAxk6i3bClg0PKmjRttwBABddgGFwVYmwSlEpCZxmi3s885F1oJcdXWbQUMFejVO49cVBliY+7DawXNcqScCOXlCCVEGwCUQcAwhJEzmOW96PBUbRHnoKDLB+iPZsA5+GC2E6t"
                + "UPfsMXGDF8uVquLoZtb+UpY5iLktGRhrOc2auMW84qQGHil1nMEkciMgGK2UNURvPYZOpsEGksSZX5T6KKEdNxD2c143EC7gByjqhABtfW3uJXZXh55+Xwq5BD2uOenmrlPgKFGQCZFNzCDlYACpzjmbm7ddBbJvdFtdFuADuxtyWEzX/9tXX35TzNQmqRWoqwbrjj6HJ3VQMYp9ChKHgDHJaAJqRg8PvQAAmM89wME7QRQq8d5mswGZTTdDmW8xU8HnyyOOOk4cffFAunXKJsrxslEOiTLRJwE7tTtoprBrWKEaykcobCiIc39bpMgVd/ah3gdoX+pqZ+YjvwIBwZmI7NGf2kNcgNHhnUS+M1s3Bao+orWFcCjADraGdu+TAlkH1vFv3blKkhIr3+hRGpQ1bHIDEZsh+qrPvvTMLJa84oMccMxw5rgww2qt70GgXITMb6LjDkQQSBqHifHIr7Sq6/sGZ7Zq2Xk8UQhLhSQZRwUL6kpuJYqFmVRjfArYPVOZXaGckY/0QGJMAluaryft8Xd3oLw8+oAHHw0xMQxDqJWaDcx0HvjeORgdBAK+gY+EFNWc6QUawRiIM0aY5ihx0vcMRRqRqi"
                + "hkMWwXEByPEjJ8IeSMs61jJiPOgeU4P2DcGD2AauAjXgDud3GGlBhD97e9PIPga1lSo1LgWdiWKMohEEDI1OarSIS4sSALiznaR2PAbEAW0NkwkiBa0Adckl3rnzbeQCqKifLxfOejrEfaNZiFWsFtcEFKZOPNAtHk9ZiPSAGl/GKkrEuX17s1O1s3EeMRZV7oVJca6ymjyyGsb1hkGtoC/BS5rFGfBykxIsEbEFTgJZi5iMu3aa404x+EJAy8hjoA7UPQF10BJyH79+lN1rmKbQ9ahZzCB41QMUcWEuu1n209Vv88FWhaiY6dOuDaIGbMbrB9gFqmiwFhIKcA9cY5j02GuLCPcJWDAqMvloVuC3zvr/SsR/6Rm+e3gzGjzm+rSr2m2xBEM0iP5d8wkeAxBFHNmz4bVESFtNpHJnsu4A93QSXCbA1DpkfYJpB+auAxih0ikzvPSM9JY0+vO2+9AFDdAGghKvbPfAg/gHBARZ5j19UQasA4VLy7MShArCBqz0SQ4B+lTEQER2Gh4AFFWPibnw0WMhgXxSQ6iZTKRcolrIH4Vz2VCBqPgksd73AtYxrrhrVhhWKDLxYBoTBZjAQXJ0OtLDejbhV/Lj98v0rKTY2TQkKGYjA8HjIptrte81luh6zg2druWePpIVx2YiAX1TjrlFM7EW6ZNY+eccurJGHjjWPIzA31nYRGSolkKySVMzkYHgtUjVgKsF4R"
                + "h7RWmBmgUc2AfeOB+TUWcAuMX9ztuvVUSlSjeeOtNmXrF5Roo9Hvp27ePEuBuzrZDxWHj11RpAWjBeZBzws9IEMQVBINYrAcTgaF/bieOlO2kCEEFnqQEekrxFdRMqKb6AAgZhP0H6iYMVwZcU2yAo5C4GLVWXcPJYMuGkcgN5yos3Kl4qAChj1jV+ltXOLwG3zfnJTVsWP00rbU5EXIZMQ2wXgLcwd+B800JKKuuMSpsj34HcYTvUI4R+GL9+o2yXdlmxw4ddMCCJvKbKF6wzftynnwx90v54ou58rqWQ/jjn/4kPiXEBx96iJ7LL+fORUAxxA8xiVm89xfxR5iOK+IJbfM2xGlCO7L2Dw6e38/FDDnALss96XuhWo1ZzHviEzjprGvBKRQjUsmlNZjjCk7Bvoih7ycOk4be11CQvzO/YbwJI9YX//A923jsyOOwbo3+zneDKbnQrJbUgJp1uBWn0UEfKuudWF5WBh8Dqf2vjz5KTtKBg13n/MIsUmMNNw1+slrK23XqAk9NSYWhDAMGpxk76jG9FsTGTdNulH59+8rtt9+pa78t5ADcpYsQ/9fUqTTPT1EP6DHqMgcrxsBFYJDDiRnrqIeNgkCwVas2qF8OrYi4wmPM/yg/aQYPz2ZVctb6ytTIrColdNg3ALrxLGY3MS5uWnptKADdCbAOe7y00ew3IQe4PryvdukPcimPG+IECxEi/RKuim81mWoEnq3Rppzz0qbFHHxoZ6dMFBcH/FokBiPQBdZCBMH07NULhd1J7ZFEGTLplOhkzA5cx1g1KYpsfEbkDNXFeqjzX6zAb4pW1znzrMmSl9eLkdvvv/cerJFQnaFGYiYasWR/H7KvbTvs/cCVoNIylyWrVWsEMJEwaNQ269VC3OGtCd2zmAvPzHP3lJYwaMiWcrLfW+Dp5yKEmYgDAQdCf5BoyvaiKhITwExQMnGMCUCidZYc54QTx/E5i4sLEeg0FfdojgsAYkAPRzCwYexUWfq4PvQNYMWoj4GgFzwgQuqcmSrgGrBVgIgI1Kxjjiwei/UYB5OxhvI47ZabsUQHxBXTCwb27SfvffghTPAgQP2sL3w8XJT4HLVS4nfgVrh2owo+kR5R"
                + "s2xoEICUEV52ZUfr9OKpYt9bEBnidyHghXgmKkFE0kZhjWw83ww40i1M+SjjZ2J0mRG5BM+ojQ5VFfVNCIKrqrhKJgiKsavfKCA9ZeJpryveWYf2NkfiAOv7JTwyTdnhFXv2VCTDowouEMG+bSwFxchuJQysBIlcj7ABt2CvAKfQdgw3MYlCyRo087XcqiAXEd29euXJNQo8URsM6vMnc+bQMtt/wABqK59paag+KnqgRmKgnO0gPwvAITASB48sPrCfA+YQAo4OYfBo/Tb8npFxOqDJao9RB6E3Xb+zhEOuyiPVHuFnONhlz2kUg8bhN9wTVlGIMRi+MAlxHr776IP31c+UHdIKx9fAqnuErUkjwX6RdtR+cJ5Wy/tEO8+sdUZvK3enlmgJjFcAcSjiZmuOgkBsTi1Zua0uyEKzBQWwI2DlImKLs845m2x8546dzC4DGIYV8uWX/qWJVpfg2iAMcA874IfdfVi+cx/LYWPgGnEI3c1g871T3huzH9yJ/REXC2diDjACNCsQsFHD/fgNOWcNlk01K2abJdSpIXH9WOIwxt6iPjy/03dQg7EeLtXlMyafOVVDBCpMkbhmyjmOULVOv/7Up84g9UOcriAVkdomiIVGKrOeKj2McHljJlMLwGYCdDBbzIDyilBLGVB8/OjRkMGMPJ/54ouyWe0dd959F2qOQYVEOSg4ybQjJ8u7mozdpWsOZhwJhKIhJOZoiYP+FPhw4AqHrYarLOnwO0nuBIocaBiprNuAhBkXmwzACNUbCwBAHDDV8rVX/iUZmVlY21aKi4pklKZv9ujVCxPBgE6GBKL6E"
                + "DglOKkNdjL4mO57bfd+BhSN7tLla73niwf8K813RWpQ/ZGcL5j15yrL3h3rjUpmKoKxcXi9UA3djCpnxbyqChPtRcAYsdryQZVVDOdxweMLVsw2JCv4nHbrzcAvNCzNnPmi9FIwuvinH+XRhx9RL+l2zEqs9oT2kI0bVGmsnLRiA+hRs9mBskuJrC4MEQMitj4Q3p+VE6Pgx6CtAi3kNSsrq8EheU52dqb845kZ8vLL/4p0DBDsDh42lB7mIDUYuhKg0iIwCtczJTyp7pNQgiFyJmhwDdpHpx8BhDYXO4fnSOQjHp+vXh9ugg7aIrBLfXrMV7O+G3BEDLQEyFbjca1z0iOd4iuRG4lp5MhRsKyicC2sovCa8rxTTz1VlmqK5IhRI+W0SZMwAFCFCTBLSstohHJ7wtAsrBOL97NiEkuQQQxCXGGpDJzDIBtj4rZpDSFTt105nQk7DBn3P79jHm0rjV3pltONWfUpOuu/mveleH0+VjR21Nsgdgw8svkMSA1wEgXNOSYbD8FMp6ko3uekkDpbs7NzbN5ccBSaTfBGvd+jqsmwA8WAOZNYDB+EDkwm8moNu3axg8R1iGcVMwqONswymJfB5u3n0DQA5mhIYmyD05HwmPJ9GPdUVdsGMnPAeT9oSl4QBrLWkQfDOmBh6xgzajeO1ixiNREOJKO0gvY1sBM4FdRVtveaq66UbuqQm3Lp5bKrsIgcweb/eA1+qzfttYavALASwwpaPKDFgG8H4Rx5o52jaYljXf4RE6tsJ9oBeksf+2wMBAdEmKEFkAkDEOQt4jIwU5wAXw4YQWxE8S5ez0a34/fWHmJd/hBLtlQkrm38Mj4QFIgAv7EJz9avYrPdEB6AdALeN693X3iDTa6ty2ot1qiFI37HYySRWBXTg5TQsIAo1Rd0q1ZbHMk12nbv2oVzTRwIRYptHwYGNg8QPUQwLLxfaL+Ms1pb8ycOco4tRxX7ETpgq/heQd1QVW1RBwvAzJQsiENBWnSQWUmpxrrkMZg2S65RUBA714gK5pFACEXmsDAlUe+HoGNc2xa5"
                + "Rcdb9biREzFkV4HE2ikIIwSQZQJ4VRWvdTC34B42R0dl5WvYQCgy4gh077/vXl0i43JkwxH0OqtY2kQmWmKtQw4EA+Jfo/3RKyLY+CiIowkxx9GvCMRBHakGopUa+9B948YNcvLJpzAOsnxvKcAatBE44bhyM2UrOQwHz2oO3JxKOwEUcKV9gZtELuZXD6wBPMICsVwvPyInRpy4kIPtGEGqlhoS0J1VeTZu3ASOxsJsNdRSHIIIRxCJDXUkPnG4CbgeuY+wllcSfD/2O0toIFYQEhrO2FB/HZc536nBS8fYojQRW7MHpL+mwegov/bHEJWxS7SuVs7naqRCQvJxquLBugifBrQLBBQj4wvslYNpyyA4m10WyxIRCcWKnZDJj0HYPhbpBSiFKIkM7Qv/u93ksoQbwky7zAqFsBgfQWNcfCIwgjWHO3vYHIORnwFshpGjg5RItA/R84xUcwApfSh8zfbBN8WIOSlOT88YpKKt8tcSRtOHCR79hpIBlb379h3YOjv7BzV3537+2WcMOB6ttglssHQiRK+TonyAy4PROY72JY1UUFM5UylmTEqlmMFDgFBNTZ0U797NIOSGen8kx7Diyby2ycwO0EQ2P4rAosAbPKkQLdby6XANZ3c+s+mKQarcuiyqhiAOo8OwxBEpTgyozwf8Q/e+0vn29JYthyogLz4CYTRfbWXV6rVHSyA8n1bBYBBYQUVq9HzN8xj63jtvawG4x6Ch0FBFjUE7sVPHjuA27HDDPyLuaUooiJP1RRuGyf+wy5EzWhxGNX9Qau2acmGHgACQcbTeV5rEjRGKebIG+KLYCzgVzicR8DwHiNrXDlfA53C+cVE/ihS/NXLZQaekpDcaebEQe/nqNBymXGTfEQijeWOO3NweGgC7HQMJreDoiESA4OvrVE4PU+/i7P79+5/y8ezZCCdkLS8Mb5W6wLeqzIeXVc+zKUqWyqyKaddbMSopsYmtC2IHmANCjQY5JxhIEgGJC3kfAMoMMWR6AAaPE4dma2tbMeLLY0RTBBAl8DTvDQgNcGBBnDwfeAW11/E5wxz9"
                + "JuwvaFRjGM+ifVELVZWfoH1Zg+v+qgxEtwfqfJOLFSbunHTyydKnt7rjt+9E1tXREgk6HLPy1F59+jy2ds3aGz768AMZPHiI5KmzLC01TWoNF4F7m7PciT42EVJhqKJWtUWdDBAJzfEuAz4DJq+EflFnoT9cB2mWqOpjlj4Xa8fA0XAjJioDv0B8GZc9fx9BDJGYg4NufUGIarNLqUILYhSZxT3+ADMA9XsW839en3GqwXJH3Y+Mfjf2lO8XfSdnnH5K08Zz/OEP18mJY0bLQw89jLcgEgLAozHvukykd8eOnW7UtIDzO3Xu0vDG66+ry/0tOp7i9IHpUbULAxoZjj3Ao10WPAgPLhfpe0jDB19Rs/WH6sHEIj7fqf9l06aNEgqEGJtJddGkBmCZDd2AJ2DSRltMARgWu6XBrrqqEoCSM99HbGBEBtuiu5MIDk7GvNkAYzC45poxfcfpjM6ANZVE53ZyfgF0r9ZnnPorF1W2izMzs/8V9eU8/NADzSLjjQnCt956i4wcMVweePAhNrJ791wQydE8KGYWor3f0JUQc8efdNKCDevzOaMLi7gEFrmADU5mrgsTisG+QSQ8YqBMtb80qserVqyE7wa+F2gYKKKL1AHMeLQPrBw4AMV2dd9Ldu+liHJzqaxtBQXIUQEIhXiBtRSEYtaWqWdbuOP+ZoFko3IDW5j1VDwkqHXr1iAOAyIO2grWjgGHW6bt6K199g9TWuHXEAa5qsbtyozpT6iTcVaTayuNN6qft992q0ZivStPPDkdhcwQ6o9yzpiRkO1HAqoYBHg4t+iaLcdrBZ8/6UA/qg/vAeHoaxAe2DuKrrHDbQ1QW36geHcxk5IGDRkML60ehxC/JCXxPKrKuwoLdZAWqu0jhwvdlJeHGSy8efMmzG7jQg+A5dOLnJTcAhZTXJ/H4qJC5viK0Lpq4Q9D/dw2osyUlQCR4f3qVStgL+ESXmVIaiorhaHvASXc263Wgu1XEgYTtJ984u/y0YfvN2FqwpE3OsKGHzNUxc3xco+60DGoffvkQTQcqRPsEuWY5Tj3b9qxXUXkHchRriTN8Lg9IAawbMw+mwBtZy09qYU7dsL"
                + "aCO6B+6NGKgYbiUTwbSCuFQnN2tbFIDg6/NIzMoUD7fEwsLh16zaot4U2wVtLbYMlpLxelHsAeAXBGM2Hwc+4l119Cbm9WCQA68LCFK+E2FXy9L56+mdaH7VvZlar2/Gshrv+WlHCdj75979awmjWxGE3lpy+T83FEDePPvY4BhCLAh+NgQ0dCfv8WSrDJyoWWBWvgwtCgdiARxaDoIY0AmH6XkzSNQZjhxLIli2b2Y6uKuaW/vyzemt/pkm6j4YVdlar6f333qsDSMyBmAoQCSyztrA+wWoDzewhU6pbaEbHFjY13OvrbeGXWuvTgejS+y2RD959m4lHgwYPRQ7tZhWKF7VMz5igomQFCO7XbRZ8xmLRY1UKnpLZH33YBEaw375RrEy76UaNbXhaZuhSXOPHjVVZXy+F6njap3L+yHYSF/bZOnNna4zoZUoE1+ug9BQJIzgHrB+cBoMOfEE80qdff3AVJjGvWL5M5s+bx+SpEerm//Tjj+U1LYGQv3YtiBWaDrkS/RrWJxO5cLLYpC1nOVP1kMLmQY2jpqEGnAW4hiD0888+YRBSHy1sx/LWLbM2q3f274ozZoTU/El7iIlc/w0cA1xRMcaTKsbfaWIL6W/fWEJ6wvhxcsIJJ2pS8TCZoonWvXv3xszGqtQOIRwej9gKvjOVo8zUmXw27DwKRE9QTgH5jVnO6yDqHQ49JHE3mDwSlGU89/wLUXlH5f9qiVdC6d9/gOR06waVFxZbaisSkWYQNioqbCLUjkzcBwralwT84BRWHQbotFoXlhWnyT0xIeF7JdwXtF2aYF79m4jBPCMnQpSPNc0"
                + "UY/xN3n+XhPHfnzjsNm/eXO7/1Gq+V145VSZNmgzgSpBZWFhEuX64zeB4Y52UWarCztIz+/tDwQtFZLKCyXZg+QDGuAbUWoQG9tUZzDKNlRUoooIkKt3bQ2OAK58gmDVETEpAlDr9avRzhBuCg9hqO04EuYscgnmsDO8jOGWsSGx87O68Vr3fVy/zq0pQ39UECGx/DWFYhx09wmjBou++VbV7N7nl2rVrZO4XnzdT38pv31ha4M9/vpMLBp951lkyWZf/6q1YYOOG9f+RSscOD4eXaicuVWvmDYoNxikRnKJY5EQdqK4uDnYYRIdOtiZzAEUYnqiK2vXzCxSbILc1J6cr7Q/RMbEQUeQEju2B8aPC+BOkTogHEdRSEwgWqJo7Vy8/R1XauVWVFXXM83VqJHJw/9PNhhGQKFi7dLF88vFshDw2geOtaTfW83rm6Rla5P45mT7jGbnooou4Dsp/GEBrPbZhjfn4TPHAZ5p1jk97qiVzlIgMUO1lgIqEPi2UELhUBa2aBJbQLqB2wo5Cz23RzkIWcWmvNdqHDDsGEVwgCGMCJyiF1rVa7SNLQ37/Mk30XhAKBZYzz8bJgfm1G4AtxAeIlkQxZ85HzKZvis0rzWcj0Ltq6hUq/8vkmmt/h5qeEAUYmKNx6lnD0xrlFmt0tGCCRzxnO8UjXTWZuZ36YdpjV1DbUm0RiRoSENd/wEDfoMFDXMo9wggDUU2gRoFqldopSsUlW5Xwtiv3QMXEDSp6CpyVoN2KR/zy2zdqZ0gVJZB+4m9/lZ9++uF/tXO1R4rjQJS7uv+jjeB0EYw2gvFGgDcCmwiACDARABFgIjBEAESwngjWGZw3gjtN1Zvqri6NDq0/4Lb0"
                + "qrqYoa3utvTcaiQBKR9pVzYi4veHjCoikiMikiMikiMikiMikiMi4o8b1g3ODsXVSmHFWNkI3cFKaSW3ksn30d5l88ukL0RoK9+sfPqPldjOmeOrlcZKAjlZKaCrraytGOgaIgBeqU0pbLbUxvroExErK8pKPui0gkFc4vUNjdBfrOygM5LBIM/WYfMVtmaw3QMiBCmmw5KDCFJ6HG5xjbGSsIyhoHMhA3F6RASIUWI8UitqjIL0wJxrB3mO0M9ZattBJwDioE1/iED/71jfpmNsvNUQA4IUQr/G+yn0xlNLzBm731A5GN6+t4evF0c8S39RjHYCmMoaqXfYP2HaBBCnB4ipZgMz91x7hR/XQ+LvEzcSEXtOfR0IVKxeEchxROH7xI0KeitEHgENvXbZhiycN056SYYN8yntGit/Q793kNCImKnad8eS0vVki7WR962YrUpcXyC2M8Ud1CcSe+6f3bf+ibEPXuc4gr36g3S1E4WqC3PYaYSiFDWMxIXZPDmIMwM5GmfGo4zSOvQXHrP4BLXhJOBPubDVIHs+yQzI4n0V1xf4CJ+AICqoT9yFaOmINR265pC1ReZMxYTMcwO7SX/YwG856RGCZCbg4XkN8kKxayurjoVozeNmpMyGJofMDukHU0PjKVxT6C89VuYJ5vkh0dyW0mUGDC/2Oxei7kxvIIOSQ6bpuSPANQbfdbOrHrPGAgOCBbXeYaC4eMiRQFJIKOQUrIIHkeLQIMhZyDuysfZWdpzp4u/yXY9gFV/3gL4rNhAUdt0hbCkQr6Ws5CXHFG06I9gODfwRsa65sPjTUc6QIpCNKIKQNSyo4NTQbyntdQdbsf2GTwCf4a8rpuho2gvyZ6WCE+vOK6JfPdP1CvEZK/WgmUMWpu6sQNkDgaXQd4Vc51AgSGcg5i+QbeB0tezw5FOb8Hosp/FwQy5QDkkOOfgJsshB6Ev2sfeM/5tJv9iiMw2mgXuiZlOeCa8ZMIihkItc/oI3HYkcVJiCAFtPdtEIsH9QQZpD7gmFAahDBxiK9U8WoqcbiNtQfIOTgxjp2UNZy4WmG+fv3FP8vWEqSDhjT23uaZdB4dUHxJkIYmwcxNDM7jOfQkCkim011A"
                + "F9kgQU5ZqNzwr+On9vxf+NM1oa/uyZnyuQSKZM/z4Cbef791YI54C9FWQbv94Tpw8nZFH/3grd4xV90wb0CZu6/AemPNde7dgXncnxSyIi/CRYRMT9yRERyRERyaEmj4AIdY+fYFh4HP9pRctKuYPdpueV1PSDar0YabA21D+Dnhvdw8eFLalvx8ocBUT+f+i4wjnFDRWQ50m/OFp54jGPmO1a9M/QKEEKTsqXsaaV47AdSBioM384OvPXBU6yjUWO5sal3AqixNRR0C6uFwYKjesX4lxkymyZQB8y1pb5ydH+LFYiDdnFsjNdmzNbBV4VdBAnUofNBPeZsDhSbxxkawPRMqtLn/ChRi5IERixNWdBNni9giAuZIJADXt/iZSZIFUWWJavgnxQ2z3FS36QTWasvWLEXLKNvSOub9ku6jNeK7EyaxyknDKbcysp2hrotsyfproFbbD9zvpjCWnF6vELI9ac98+ovwkmjv69MnZmLMAnzxL7Ae2NmApOaHNhg1OQzyAfV9Y2536ga0QtkooNrU9sGtxhII+wtXQQ7otj/yUT0+aOHdRp2f3WIFkKf/K+lbTFySGJAtIriB6PHH7owHMRtc8WOvPYxQdQol3j9edv/x0ZDHaIBAE2W0+a/8EGtHZMUzr0zAfkYRbBavGUqo62Mm6ro49VqD/HcYQ9y5g1CKJFG2lzKvTXiRvP8FE7vtaoyB/Z8hDjCQ9QMxg54GgKydmhVoMbMExv8GSt2Jwpn64F2syFLmF2EkrBKNxYQXiDjxR2XlihVnE/0Gn2xaGCpfqK+ePYoc2FEWbLri9E/+SINaFT86gxCBn0C9irZRwQmpLIlwLhE+HzDYb505xU996VVQiugXRFQh03iA8J45zyqJNL+T6Rxn/yS1xzRg3RMGLIOJRjPcPAjkI7AdGW+qnF2A9AjogFHYXsBWdklsv/d8s+QqEYbSF9wLBMpB/9Z58i/Cu6fw1gd+ZQ/PI/bx0Rz3NERHJERHJERPwLZ401JHjcCs0AAAAASUVORK5CYII=)}");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCssEmbed() throws Exception {
    runToolChain("cssembed", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String css = result.get(Type.CSS).getContents();
        assertOutput(
            css,
            ".background {\n  background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAIAAAD/gAIDAAABaElEQVR42u3aQRKCMAwAQB7n/7+EVx1HbZsEStlcldAsUJrq9hDNsSGABQsWLFiwEMCCBQsWLFgIYMGCBQsWLASwYMGCBQsWAliwYMGCBQtBCGt/j1UrHygTFixYsGDBgnUE1v4lKnK2Zw5h7f0RLGmMLDjCSJlVWOnuYyP8zDwdVkpVKTlnx4o8Dr1YLV8uwUqZ4IP3S1ba1wPnfRsWvZUqVjMnYw1ffFiZBy6OlTvu1bBKZ7rc1f90WJGILx3ujpXSD94Iq/0ssLpPtOYEX7RR03WTro8V2TW7NVbvImOuOWtyr6u2O6fsr8O6LNY8T+JxWEd6/SisGqvlFFvpZsvAenrg0+HBl2DFO97g5S1qthP24NsTVbQ+uQlTurT/WLnNxIS/rQ2UuUVyJXbX6Y16YpvZgXVK41Z3/SLhD7iwYMGCBUvAggULFixYAhYsWLBgwRKwYMGCBQuWgAULFixYsAQsWDXxBFVy4xyOC7MdAAAAAElFTkSuQmCC);\n}\n.svg {\n  background: url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2aWV3Qm94PSIwIDAgMTk1IDgyIj4KICA8dGl0bGU+U1ZHIGxvZ28gY29tYmluZWQgd2l0aCB0aGUgVzNDIGxvZ28sIHNldCBob3Jpem9udGFsbHk8L3RpdGxlPgogIDxkZXNjPlRoZSBsb2dvIGNvbWJpbmVzIHRocmVlIGVudGl0aWVzIGRpc3BsYXllZCBob3Jpem9udGFsbHk6IHRoZSBXM0MgbG9nbyB3aXRoIHRoZSB0ZXh0ICdXM0MnOyB0aGUgZHJhd2luZyBvZiBhIGZsb3dlciBvciBzdGFyIHNoYXBlIHdpdGggZWlnaHQgYXJtczsgYW5kIHRoZSB0ZXh0ICdTVkcnLiBUaGVzZSB0aHJlZSBlbnRpdGllcyBhcmUgc2V0IGhvcml6b250YWxseS48L2Rlc2M+CiAgCiAgPG1ldGFkYXRhPgogICAgPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIiB4bWxuczpyZGZzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzAxL3JkZi1zY2hlbWEjIiB4bWxuczpjYz0iaHR0cDovL2NyZWF0aXZlY29tbW9ucy5vcmcvbnMjIiB4bWxuczp4aHRtbD0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94aHRtbC92b2NhYiMiIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyI+CiAgICAgIDxjYzpXb3JrIHJkZjphYm91dD0iIj4KICAgICAgICA8ZGM6dGl0bGU+U1ZHIGxvZ28gY29tYmluZWQgd"
                + "2l0aCB0aGUgVzNDIGxvZ288L2RjOnRpdGxlPgogICAgICAgIDxkYzpmb3JtYXQ+aW1hZ2Uvc3ZnK3htbDwvZGM6Zm9ybWF0PgogICAgICAgIDxyZGZzOnNlZUFsc28gcmRmOnJlc291cmNlPSJodHRwOi8vd3d3LnczLm9yZy8yMDA3LzEwL3N3LWxvZ29zLmh0bWwiLz4KICAgICAgICA8ZGM6ZGF0ZT4yMDA3LTExLTAxPC9kYzpkYXRlPgogICAgICAgIDx4aHRtbDpsaWNlbnNlIHJkZjpyZXNvdXJjZT0iaHR0cDovL3d3dy53My5vcmcvQ29uc29ydGl1bS9MZWdhbC8yMDAyL2NvcHlyaWdodC1kb2N1bWVudHMtMjAwMjEyMzEiLz4KICAgICAgICA8Y2M6bW9yZVBlcm1pc3Npb25zIHJkZjpyZXNvdXJjZT0iaHR0cDovL3d3dy53My5vcmcvMjAwNy8xMC9zdy1sb2dvcy5odG1sI0xvZ29XaXRoVzNDIi8+CiAgICAgICAgPGNjOmF0dHJpYnV0aW9uVVJMIHJkZjpyZW91cmNlPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL3N3LyIvPgogICAgICAgIDxkYzpkZXNjcmlwdGlvbj5UaGUgbG9nbyBjb21iaW5lcyB0aHJlZSBlbnRpdGllcyBkaXNwbGF5ZWQgaG9yaXpvbnRhbGx5OiB0aGUgVzNDIGxvZ28gd2l0aCB0aGUgdGV4dCAnVzNDJzsgdGhlIGRyYXdpbmcgb2YgYSBmbG93ZXIgb3Igc3RhciBzaGFwZSB3aXRoIGVpZ2h0IGFybXM7IGFuZCB0aGUgdGV4dCAnU1ZHJy4gVGhlc2UgdGhyZWUgZW50aXRpZXMgYXJlIHNldCBob3Jpem9udGFsbHkuCgkJCTwvZGM6ZGVzY3JpcHRpb24+CiAgICAgIDwvY2M6V29yaz4KICAgIDwvcmRmOlJERj4KICA8L21ldGFkYXRhPgogIAogIDx0ZXh0IHg9IjAiIHk9Ijc1IiBmb250LXNpemU9IjgzIiBmaWxsLW9wYWNpdHk9IjAiIGZvbnQtZmFtaWx5PSJUcmVidWNoZXQiIGxldHRlci1zcGFjaW5nPSItMTIiPlczQzwvdGV4dD4KICA8dGV4dCB4PSIxODAiIHk9Ijc1IiBm"
                + "b250LXNpemU9IjgzIiBmaWxsLW9wYWNpdHk9IjAiIGZvbnQtZmFtaWx5PSJUcmVidWNoZXQiIGZvbnQtd2VpZ2h0PSJib2xkIj5TVkc8L3RleHQ+CiAgPGRlZnM+CiAgICA8ZyBpZD0iU1ZHIiBmaWxsPSIjMDA1QTlDIj4KICAgICAgPHBhdGggaWQ9IlMiIGQ9Ik0gNS40ODIsMzEuMzE5IEMyLjE2MywyOC4wMDEgMC4xMDksMjMuNDE5IDAuMTA5LDE4LjM1OCBDMC4xMDksOC4yMzIgOC4zMjIsMC4wMjQgMTguNDQzLDAuMDI0IEMyOC41NjksMC4wMjQgMzYuNzgyLDguMjMyIDM2Ljc4MiwxOC4zNTggTDI2LjA0MiwxOC4zNTggQzI2LjA0MiwxNC4xNjQgMjIuNjM4LDEwLjc2NSAxOC40NDMsMTAuNzY1IEMxNC4yNDksMTAuNzY1IDEwLjg1MCwxNC4xNjQgMTAuODUwLDE4LjM1OCBDMTAuODUwLDIwLjQ1MyAxMS43MDEsMjIuMzUxIDEzLjA3MCwyMy43MjEgTDEzLjA3NSwyMy43MjEgQzE0LjQ1MCwyNS4xMDEgMTUuNTk1LDI1LjUwMCAxOC40NDMsMjUuOTUyIEwxOC40NDMsMjUuOTUyIEMyMy41MDksMjYuNDc5IDI4LjA5MSwyOC4wMDYgMzEuNDA5LDMxLjMyNCBMMzEuNDA5LDMxLjMyNCBDMzQuNzI4LDM0LjY0MyAzNi43ODIsMzkuMjI1IDM2Ljc4Miw0NC4yODYgQzM2Ljc4Miw1NC40MTIgMjguNTY5LDYyLjYyNSAxOC40NDMsNjIuNjI1IEM4LjMyMiw2Mi42MjUgMC4xMDksNTQuNDEyIDAuMTA5LDQ0LjI4NiBMMTAuODUwLDQ0LjI4NiBDMTAuODUwLDQ4LjQ4MCAxNC4yNDksNTEuODg0IDE4LjQ0Myw1MS44ODQgQzIyLjYzOCw1MS44ODQgMjYuMDQyLDQ4LjQ4MCAyNi4wNDIsNDQuMjg2IEMyNi4wNDIsNDIuMTkxIDI1LjE5MSw0MC4yOTggMjMuODIxLDM4LjkyMyBMMjMuODE2LDM4LjkyMyBDMjIuNDQxLDM3LjU0OCAyMC40NjgsMzcuMDc0IDE4LjQ0MywzNi42OTcgTDE4LjQ0MywzNi42OTIgQzEzLjUzMywzNS45MzkgOC44MDAsMzQuNjM4IDUuNDgyLDMxLjMxOSBMNS40ODIsMzEuMzE5IEw1LjQ4MiwzMS4zMTkgWiIvPgogICAgICA8cGF0aCBpZD0iViIgZD0iTSA3My40NTIsMC4wMjQgTDYwLjQ4Miw2Mi42MjUgTDQ5Ljc0Miw2Mi42MjUgTDM2Ljc4MiwwLjAyNCBMNDcuNTIyLDAuMDI0IEw1NS4xMjIsMzYuNjg3IEw2Mi43MTIsMC4wMjQgTDczLjQ1MiwwLjAyNCBaIi8+CiAgICAgIDxwYXRoIGlkPSJHIiBkPSJNIDkxLjc5MiwyNS45NTIgTDExMC4xMjYsMjUuOTUyIEwxMTAuMTI2LDQ0LjI4NiBMMTEwLjEzMSw0NC4yODYgQzExMC4xMzEsNTQuNDEzIDEwMS45MTgsNjIuNjI2IDkxLjc5Miw2Mi42MjYgQzgxLjY2NSw2Mi42MjYgNzMuNDU4LDU0LjQxMyA3My40NTgsNDQuMjg2IEw3My40NTgsNDQuMjg2IEw3My40NTgsMTguMzU5IEw3My40NTMsMTguMzU5IEM3My40NTMsOC4"
                + "yMzMgODEuNjY1LDAuMDI1IDkxLjc5MiwwLjAyNSBDMTAxLjkxMywwLjAyNSAxMTAuMTI2LDguMjMzIDExMC4xMjYsMTguMzU5IEw5OS4zODUsMTguMzU5IEM5OS4zODUsMTQuMTY5IDk1Ljk4MSwxMC43NjUgOTEuNzkyLDEwLjc2NSBDODcuNTk3LDEwLjc2NSA4NC4xOTgsMTQuMTY5IDg0LjE5OCwxOC4zNTkgTDg0LjE5OCw0NC4yODYgTDg0LjE5OCw0NC4yODYgQzg0LjE5OCw0OC40ODEgODcuNTk3LDUxLjg4MCA5MS43OTIsNTEuODgwIEM5NS45ODEsNTEuODgwIDk5LjM4MCw0OC40ODEgOTkuMzg1LDQ0LjI5MSBMOTkuMzg1LDQ0LjI4NiBMOTkuMzg1LDM2LjY5OCBMOTEuNzkyLDM2LjY5OCBMOTEuNzkyLDI1Ljk1MiBMOTEuNzkyLDI1Ljk1MiBaIi8+CiAgICA8L2c+CiAgPC9kZWZzPgogIDxnIHNoYXBlLXJlbmRlcmluZz0iZ2VvbWV0cmljUHJlY2lzaW9uIiB0ZXh0LXJlbmRlcmluZz0iZ2VvbWV0cmljUHJlY2lzaW9uIiBpbWFnZS1yZW5kZXJpbmc9Im9wdGltaXplUXVhbGl0eSI+CiAgICA8Zz4KICAgICAgPGcgaWQ9ImxvZ28iIHRyYW5zZm9ybT0ic2NhbGUoMC4yNCkgdHJhbnNsYXRlKDAsIDM1KSI+CiAgICAgICAgPGcgc3Ryb2tlLXdpZHRoPSIzOC4wMDg2IiBzdHJva2U9IiMwMDAiPgogICAgICAgICAgPGcgaWQ9InN2Z3N0YXIiIHRyYW5zZm9ybT0idHJhbnNsYXRlKDE1MCwgMTUwKSI+CiAgICAgICAgICAgIDxwYXRoIGlkPSJzdmdiYXIiIGZpbGw9IiNFREE5MjEiIGQ9Ik0tODQuMTQ4NywtMTUuODUxMyBhMjIuNDE3MSwyMi40MTcxIDAgMSAwIDAsMzEuNzAyNiBoMTY4LjI5NzQgYTIyLjQxNzEsMjIuNDE3MSAwIDEgMCAwLC0zMS43MDI2IFoiLz4KICAgICAgICAgICAgPHVzZSB4bGluazpocmVmPSIjc3ZnYmFyIiB0cmFuc2Zvcm09InJvdGF0ZSg0NSkiLz4KICAgICAgICAgICAgPHVzZSB4bGluazpocmVmPSIjc3ZnYmFyIiB0cmFuc2Zvcm09InJvdGF0ZSg5MCkiLz4KICAgICAgICAgICAgPHVzZSB4bGluazpocmVmPSIjc3ZnYmFyIiB0cmFuc2Zvcm09InJvdGF0ZSgxMzUpIi8+CiAgICAgICAgICA8L2c+CiAgICAgICAgPC9nPgogICAgICAgIDx1c2UgeGxpbms6aHJlZj0iI3N2Z3N0YXIiLz4KICAgICAgPC9nPgogICAgICA8ZyBpZD0iU1ZHLWxhYmVsIj4KICAgICAgICA8dXNlIHhsaW5rOmhyZWY9IiNTVkciIHRyYW5zZm9ybT0ic2NhbGUoMS4wOCkgdHJhbnNsYXRlKDY1LDEwKSIvPgogICAgICA8L2c+CiAgICA8L2c+CiAgPC9nPgo8L3N2Zz4K);\n}");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore("Should be moved to the server tests")
  public void testOutputOnly() throws Exception {
    runToolChain("out-only", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        // assertThat(directory.list().length, is(2));
        // assertThat(new File(directory, "basic-min.js").exists(), is(true));
        // assertThat(new File(directory, "style.css").exists(), is(true));
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testClosureError() throws Exception {
    try {
      runToolChain("closure-error", null);
    } catch (final SmallerException e) {
      assertThat(
          e.getMessage(),
          is("Closure Failed: JSC_PARSE_ERROR. Parse error. missing ( before function parameters. at source.js line 1 : 10"));
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testUnicodeEscape() throws Exception {
    runToolChain("unicode-escape", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String basicMin = result.get(Type.JS).getContents();
        assertOutput(
            basicMin,
            "var stringEscapes={\"\\\\\":\"\\\\\",\"'\":\"'\",\"\\n\":\"n\",\"\\r\":\"r\",\"\t\":\"t\",\"\\u2028\":\"u2028\",\"\\u2029\":\"u2029\"}");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testRelativeResolving() throws Exception {
    runToolChain("relative-resolving", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String basic = result.get(Type.JS).getContents();
        assertOutput(basic, "// test1.js\n\n// test2.js\n");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testTypeScript() throws Exception {
    runToolChain("typescript", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        assertOutput(
            result.get(Type.JS).getContents(),
            "var Greeter = (function () {\n    function Greeter(message) {\n        this.greeting = message;\n    }\n    Greeter.prototype.greet = function () {\n        return \"Hello, \" + this.greeting;\n    };\n    return Greeter;\n})();\nvar greeter = new Greeter(\"world\");\nvar button = document.createElement('button');\nbutton.innerText = \"Say Hello\";\nbutton.onclick = function () {\n    alert(greeter.greet());\n};\ndocument.body.appendChild(button);\n");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore("Currently the result is not assertable")
  public void testTypeScriptCompiler() throws Exception {
    runToolChain("typescript-compiler", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        assertOutput(result.get(Type.JS).getContents(), "wada");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  @Ignore
  public void testJpegTran() throws Exception {
    runToolChain("jpegtran", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        assertOutput("yada", "wada");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testYcssmin() throws Exception {
    runToolChain("ycssmin", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        assertOutput(result.get(Type.CSS).getContents(), "h1{color:0000FF}");
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testJsHint() throws Exception {
    try {
      runToolChain("jshint", new ToolChainCallback() {
        @Override
        public void test(final Result result) throws Exception {
        }
      });
      fail("Expected to have jshint errors");
    } catch (final SmallerException e) {
    }
  }

  /**
   * @throws Exception
   */
  @Test
  public void testBrowserify() throws Exception {
    runToolChain("browserify", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String expected = ";(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require==\"function\"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);throw new Error(\"Cannot find module '\"+o+\"'\")}var f=n[o]={exports:{}};t[o][0].call(f.exports,function(e){var n=t[o][1][e];return s(n?n:e)},f,f.exports,e,t,n,r)}return n[o].exports}var i=typeof require==\"function\"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){\nvar m = require('./module');\nm.test();\n\n},{\"./module\":2}],2:[function(require,module,exports){\nmodule.exports = {test:function() {}};\n\n},{}]},{},[1])\n;";
        assertOutput(result.get(Type.JS).getContents(), expected);
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testCoffeeScritBrowserify() throws Exception {
    runToolChain("coffeescript-browserify", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String expected = ";(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require==\"function\"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);throw new Error(\"Cannot find module '\"+o+\"'\")}var f=n[o]={exports:{}};t[o][0].call(f.exports,function(e){var n=t[o][1][e];return s(n?n:e)},f,f.exports,e,t,n,r)}return n[o].exports}var i=typeof require==\"function\"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){\n(function() {\n  var m;\n\n  m = require('./module');\n\n  m.test();\n\n}).call(this);\n\n},{\"./module\":2}],2:[function(require,module,exports){\n(function() {\n  var func;\n\n  func = function(x) {\n    return x * 2;\n  };\n\n  module.exports = {\n    test: func\n  };\n\n}).call(this);\n\n},{}]},{},[1])\n;";
        assertOutput(result.get(Type.JS).getContents(), expected);
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSvgo() throws Exception {
    runToolChain("svgo", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String expected = "<svg width=\"10\" height=\"20\">test</svg>";
        assertOutput(result.get(Type.SVG).getContents(), expected);
      }
    });
  }

  /**
   * @throws Exception
   */
  @Test
  public void testSweetjs() throws Exception {
    runToolChain("sweetjs", new ToolChainCallback() {
      @Override
      public void test(final Result result) throws Exception {
        final String expected = "function add$112(a$113, b$114) {\n    return a$113 + b$114;\n}";
        assertOutput(result.get(Type.JS).getContents(), expected);
      }
    });
  }

}
